require 'geo_helper'

namespace :trips do

  desc "Calculation trips by events"
  task :calculate => :environment do

  	KM_PER_MILE = 1.609344
  	MILE_PER_KM = 0.621371192

		def update_last_event(last_event_id,device,phones_log,trip = nil)

			if trip
				event = Event.find(last_event_id)
				trip.phone.alerts.where(['? not between restricted_time_start and restricted_time_end',event.time.strftime('%H:%M')]).map do |e|
					Mailer.alert_time_restriction.deliver
				end
			end

			last_event = CalculatedEvent.find_or_create_by_textbuster_mac_and_phones_log_id(device.imei,phones_log.id)
			# last_event.update_attributes :last_event_id => last_event_id
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

  	puts 'Prepare calculation trips....'

  	grouped_events = Event.select('phones_log_id, textbuster_mac').joins(:phones_log,:device).group('phones_log_id, textbuster_mac')

 		grouped_events.each_with_index do |grouped_event,idx|
 			puts "Calculating #{idx+1} of #{grouped_events.size}"

			device = Device.find_by_imei(grouped_event.textbuster_mac)
			phones_log = PhonesLog.find(grouped_event.phones_log_id)
			phone = phones_log.phone

 			trips,trip = [],{}
 			query = Event.query_for_trips_calculation(grouped_event.textbuster_mac,grouped_event.phones_log_id)

    	events = ActiveRecord::Base.connection.select_all(query)

			#  Separating trips
	    t = nil
	    current_trip = nil
	    events.map do |e|
	    	if e['qr2_next_id'].nil? && current_trip.nil?
	    		current_trip = e['qr2_id'].to_i 
	    		next
	    	end
		    t = e['qr2_id'].to_i if e['qr0_id'].nil? && t.nil?
		    if e['qr0_id'] && t
		    	trips << {:start_id => t , :end_id => e['qr2_next_id']}
		    	t = nil
		    end
	    end

	    #  Create a completed trips
	    trips.map do |t|
		  	locations = Location.joins(:events).where(:events => {:id => t[:start_id].to_i..t[:end_id].to_i})
		  	locations = locations.where(:events => {:textbuster_mac => device.imei, :phones_log_id => phones_log.id})

		  	#  Average speed
			 	average_speed = locations.average('spd')
				distance = calculate_distance(locations)

				trip = Trip.new :distance => distance, :average_speed => average_speed,
					:user => phone.user,
					:device => device,
					:start_point => locations.first,
					:end_point => locations.last,
					:phone => phone
				if trip.save
					locations.update_all :trip_id => trip.id
					update_last_event t[:end_id], device, phones_log, trip
					puts "  ... created new trip, user: #{phone.user.email} trip_id: #{trip.id}"
				end
	    end

	    #  Create or update current trip
	    if current_trip
	    	last_event_id = CalculatedEvent.find_by_textbuster_mac_and_phones_log_id(device.imei,phones_log.id).last_event_id
				 			 
	    	locations = Location.joins(:events).where(['events.id >= ?', current_trip])
	    	locations = locations.where(:events => {:textbuster_mac => device.imei, :phones_log_id => phones_log.id})
				locations = locations.where(['events.id > ?',last_event_id]) unless last_event_id.nil? && last_event_id < current_trip

				if locations.count > 0
			  	#  Average speed
				 	average_speed = locations.average('spd')
					distance = calculate_distance(locations)

					trip = Trip.find_or_create_by_user_id_and_device_id_and_phone_id_and_start_point_id( phone.user.id, device.id, phone.id, locations.first.id)
					if trip.update_attributes(:end_point => locations.last, :distance => distance, :average_speed => average_speed)
						locations.update_all :trip_id => trip.id
						update_last_event locations.select('events.id').last.id, device, phones_log, trip
						puts "  ... updated trip, user: #{phone.user.email} trip_id: #{trip.id}"
					end
				end
	    end

	  end
  end
end