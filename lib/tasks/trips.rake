require 'geo_helper'

namespace :trips do

  desc "Calculation trips by events"
  task :calculate => :environment do

  	MAILING_FREQ = 15 # in minutes
  	KM_PER_MILE = 1.609344
  	MILE_PER_KM = 0.621371192

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
			trip_alert = trip.phone.alerts.time_resrtriction.where(['? not between restricted_time_start and restricted_time_end',event.time.strftime('%H:%M')])
			trip_alert.map do |e|
				send_alert(trip, e) do
					puts '.... mailing alert time'
					Mailer.delay.alert_notification(event.time,e,phones_log,event,trip)
				end
			end

			#  Check leave zone restriction
			if event.location && rz = event.location.restricted_zones
				alert = rz.first.alert
				send_alert(trip, alert) do
					puts '.... mailing alert zone'
					Mailer.delay.alert_notification(event.time,alert,phones_log,event,trip)
					# Mailer.alert_notification(event.time,alert,phones_log).deliver
				end
			end

			#  Check speed restriction
			if event.location
				trip_alert = trip.phone.alerts.speed_resrtriction.where(['? > speed', event.location.spd.to_i])
				trip_alert.map do |e|
					send_alert(trip, e) do
						puts '.... mailing speed time'
						Mailer.delay.alert_notification(event.time,e,phones_log,event,trip)
					end
				end
			end

			last_event = CalculatedEvent.find_or_create_by_textbuster_mac_and_phones_log_id(device.imei,phones_log.id)
			last_event.update_attributes :last_time => last_time
		end

		def calculate_distance(locations)
			z = []
			# distance = locations.count > 1 ? locations.inject(0) {|s,l| z.last.nil? ? (0 && z.push(l)) : l.distance_to(z.last)} : 0
			distance = 0 
			if locations.count > 1 
				locations.map do |l|
					if z.last.nil?
						z.push(l)
						next 
					end
					distance += l.distance_to(z.last)
# puts "from(#{z.last.lat}, #{z.last.lng}) to(#{l.lat}, #{l.lng}) =  #{l.distance_to(z.last)*MILE_PER_KM}" 
					z.push(l)		
				end
			end
			distance *= MILE_PER_KM
		end

  	puts "#{Time.now} Start. Prepare calculation trips...."

  	grouped_events = Event.select('phones_log_id, textbuster_mac').joins(:phones_log,:device).group('phones_log_id, textbuster_mac')

 		grouped_events.each_with_index do |grouped_event,idx|
 			puts "Calculating #{idx+1} of #{grouped_events.size}"

			device = Device.find_by_imei(grouped_event.textbuster_mac)
			phones_log = PhonesLog.find(grouped_event.phones_log_id)
			phone = phones_log.phone

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

		  	if locations.count > 0
			  	#  Average speed
				 	average_speed = locations.where('spd > 0').average('spd') rescue 0
					distance = calculate_distance(locations)

					trip = Trip.find_or_initialize_by_user_id_and_device_id_and_start_point_id_and_phone_id(phone.user.id,
						device.id,
						locations.first.id,
						phone.id
					)
					
					if trip.new_record?
						puts "  ... created new trip, user: #{phone.user.email} trip_id: #{trip.id}"
					else
						puts "  ... updated trip, user: #{phone.user.email} trip_id: #{trip.id}"
					end

					trip.update_attributes(:end_point_id => locations.last.id,
						:distance => distance, 
						:average_speed => average_speed
					)


					if trip.save
						locations.update_all :trip_id => trip.id
						update_last_event end_time, device, phones_log, trip
					end
				end # if locations.count > 0
	    end

	  end
	  puts "#{Time.now} Finish"
  end
end