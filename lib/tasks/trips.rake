require 'geo_helper'

namespace :trips do

	MAILING_FREQ = 15 # in minutes
	KM_PER_MILE = 1.609344
	MILE_PER_KM = 0.621371192

	def calculate_distance_and_speed(locations, last_point = false)
		res = {:distance => 0, :speed => []}
		locations = locations.select('distinct locations.lat,locations.lng,locations.time')
		locations = locations.where('locations.spd > 0')
		points = locations.each_cons(2)
		points = points.to_a.last(1) if last_point 
		locations.each_cons(2) do |a,b| 
			distance = a.distance_to(b) * MILE_PER_KM
			next if (b.time-a.time) == 0
			speed = (distance / (b.time - a.time))*3600 
			res[:distance] += distance
			res[:speed] << speed

			#  For debug
			if ENV['DEBUG']=='yes'
				puts "#{a.lat},#{a.lng} - #{b.lat},#{b.lng}, #{b.time-a.time}, #{distance}, #{speed}" 
			end

		end
		res[:speed] = res[:speed].reduce(:+)/res[:speed].size.to_f rescue 0

		#  For debug
		if ENV['DEBUG']=='yes'
			puts "#{res[:distance]}, #{res[:speed]}"
		end

		return res
	rescue => e
		raise "Error on calculate_distance_and_speed, #{e.message}"
	end

	def update_distance_and_speed(trip_id)
		trip = Trip.find_by_id(trip_id)
		locations = trip.locations.order('locations.time')
		z = calculate_distance_and_speed(locations)
		trip.update_attributes :distance => z[:distance].to_f, :average_speed => z[:speed]
	end

	desc 'Update start and end points for all trips'
	task :location, :trip_id, :needs => :environment do |t, args|
		puts "#{Time.now} Start. Updating trips.... #{args[:trip_id]}"
		trips = Trip.select(:id)
		trips = trips.where(:id => args[:trip_id]) if args[:trip_id]
		trips = trips.uniq
		trips.each do |t|
			locations = Location.where(:trip_id => t.id).order('time')
			t.update_attributes :start_point_id => locations.first.id , :end_point_id => locations.last.id
		end
		puts "#{Time.now} Finish"
	end

	desc 'Update speed and distances for all trips'
	task :speed, :trip_id, :needs => :environment do |t, args|
		puts "#{Time.now} Start. Updating trips.... #{args[:trip_id]}"
		trips = Trip.select(:id)
		trips = trips.where(:id => args[:trip_id]) if args[:trip_id]
		trips = trips.uniq
		trips.each {|t|  update_distance_and_speed(t.id)}
		puts "#{Time.now} Finish"
	end

	desc 'Delete all old trips'
	task :compact, :days, :needs => :environment do |t, args|
		puts "#{Time.now} Start. Compacting trips...."
		time_ago = (args[:days] || 14).to_i
		trips = Trip.where(['updated_at <= ?',Time.now.ago(time_ago.day)])
		count = trips.count		
		trips.destroy_all
		puts "... deleted #{count} trips"
		puts "#{Time.now} Finish"
	end

  desc "Calculation trips by events"
  task :calculate => :environment do

  	def send_alert(trip, alert)
			alert_trip_notifications = AlertTripNotification.find_or_initialize_by_trip_id_and_alert_id(trip.id, alert.id)
			if (alert_trip_notifications.counter < alert.repeat_count) && (alert_trip_notifications.updated_at.nil? || alert_trip_notifications.updated_at <= MAILING_FREQ.minutes.ago)		
				yield
				if alert_trip_notifications.new_record? 
					alert_trip_notifications.save
				else
					alert_trip_notifications.update_attribute(:alert_id, alert.id) 
				end
			end
  	end

		def update_last_event(last_time,device,phones_log,trip)
			event = Event.find_by_time(last_time)
			
			#  Check time restrictions
			if event.location
				location_time = event.location.time_with_timezone(trip.timezone)
				trip_alert = trip.phone.alerts.time_resrtriction.where(['? not between restricted_time_start and restricted_time_end',location_time.strftime('%H:%M')])
				trip_alert.map do |e|
					send_alert(trip, e) do
						puts '.... mailing alert time'
						# Mailer.delay.alert_notification(event.time,e,phones_log,event,trip)
						Mailer.alert_notification(event.time,e,phones_log,event,trip).deliver
					end
				end
			end

			#  Check leave zone restriction
			if event.location && rz = event.location.restricted_zones
				alert = rz.first.alert
				send_alert(trip, alert) do
					puts '.... mailing alert zone'
					# Mailer.delay.alert_notification(event.time,alert,phones_log,event,trip)
					Mailer.alert_notification(event.time,alert,phones_log,event,trip).deliver
				end
			end

			#  Check speed restriction
			if event.location
				locations = trip.locations.order('locations.time')
				speed = calculate_distance_and_speed(locations,true)
				trip_alert = trip.phone.alerts.speed_resrtriction.where(['? > speed + speed_over', speed[:speed]])
				trip_alert.map do |e|
					send_alert(trip, e) do
						puts '.... mailing alert speed'
						# Mailer.delay.alert_notification(event.time,e,phones_log,event,trip)
						Mailer.alert_notification(event.time,e,phones_log,event,trip).deliver
					end
				end
			end

			last_event = CalculatedEvent.find_or_create_by_textbuster_mac_and_phones_log_id(device.imei,phones_log.id)
			last_event.update_attributes :last_time => last_time
			Event.where(:textbuster_mac => device.imei, :phones_log_id => phones_log.id).where(['time <= ?',last_time]).delete_all
		end

  	puts "#{Time.now} Start. Prepare calculation trips...."

  	grouped_events = Event.select('phones_log_id, textbuster_mac').joins(:phones_log,:device).group('phones_log_id, textbuster_mac')

 		grouped_events.each_with_index do |grouped_event,idx|

			device = Device.find_by_imei(grouped_event.textbuster_mac)
			phones_log = PhonesLog.find(grouped_event.phones_log_id)
			phone = phones_log.phone

 			puts "Calculating #{idx+1} of #{grouped_events.size} [#{device.imei} #{phones_log.imei}]"

			if (phone.nil? || phones_log.nil?)
				puts "WARNING!!! Phone is not defined, imei:#{phones_log.imei}, mac:#{grouped_event.textbuster_mac}"
				next
			end
			if device.nil?
				puts "WARNING!!! Textbuster device is not defined, imei:#{phones_log.imei}, mac:#{grouped_event.textbuster_mac}"
				next
			end

 			trips,trip = [],{}
 			events = Event.events_for_calculation_trip(grouped_event.textbuster_mac,grouped_event.phones_log_id)

	    #  Create a completed trips
	    events.map do |t|
				start_time = DateTime.parse(t['start_time'])
				end_time = DateTime.parse(t['end_time'])

		  	locations = Location.joins(:events).where(:events => {:time => start_time..end_time})
		  	locations = locations.where(:events => {:textbuster_mac => device.imei, :phones_log_id => phones_log.id})
		  	locations = locations.order('locations.time')

		  	if locations.count > 0
					trip = Trip.where(:device_id => device.id, :phone_id => phone.id, :last_time_event => (end_time.ago(10.minutes)..end_time)).limit(1).first

					unless trip 
						trip = Trip.new :user_id => phone.user.id,
							:device_id => device.id,
							:phone_id => phone.id,
							:start_point_id => locations.first.id,
							:end_point_id => locations.last.id,
					 		:last_time_event => end_time
					else
						trip.update_attributes(:end_point_id => locations.last.id,
							:last_time_event => end_time
						)
					end
					
					if trip.new_record?
						puts "  ... created new trip, user: #{phone.user.email}"
					else
						puts "  ... updated trip, user: #{phone.user.email} trip_id: #{trip.id}"
					end

					if trip.save
						locations.update_all :trip_id => trip.id
						update_distance_and_speed(trip.id)
						update_last_event end_time, device, phones_log, trip
					end

				end # if locations.count > 0
	    end

	  end
	  puts "#{Time.now} Finish"
  end
end