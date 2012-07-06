require 'geo_helper'

namespace :trips do
  desc "Calculation trips by events"
  task :calculate => :environment do

		def update_last_event(last_event_id,device,phones_log)
			last_event = CalculatedEvent.find_or_create_by_last_event_id(last_event_id)
			last_event.update_attributes :device => device , :phones_log => phones_log
			last_event.save
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
	    	current_trip = e['qr2_id'].to_i if e['qr2_next_id'].nil? && current_trip.nil?
		    t = e['qr2_id'].to_i if e['qr0_id'].nil? && t.nil?
		    if e['qr0_id'] && t
		    	trips << {:start_id => t , :end_id => e['qr2_next_id']}
		    	t = nil
		    end
	    end


	    #  Create a completed trips
	    trips.map do |t|
				z = []
		  	locations = Location.joins(:events).where(:events => {:id => (t[:start_id].to_i..t[:end_id].to_i)})
		  	#  Average speed
			 	average_speed = locations.average('spd')
				distance = locations.count > 1 ? locations.inject(0) {|s,l| z.last.nil? ? (0 && z.push(l)) : l.distance_to(z.last)} : 0

				trip = Trip.new :distance => distance, :average_speed => average_speed,
					:user => phone.user,
					:device => device,
					:start_point => locations.first,
					:end_point => locations.last,
					:phone => phone
				if trip.save
					puts "  ... created new trip"

					update_last_event t[:end_id], device,phones_log
				end
	    end

	    #  Create or update current trip

	  end
  end
end

=begin
[{:start_id=>2, :end_id=>"336"}, {:start_id=>1114, :end_id=>"1185"}, {:start_id=>1413, :end_id=>"1425"}, {:start_id=>1432, :end_id=>"2524"}, {:start_id=>2935, :end_id=>"3347"}, {:start_id=>3367, :end_id=>"3410"}, {:start_id=>3598, :end_id=>"4329"}]

query = "
        select qr2.id as 'qr2_id', qr2.next_id as 'qr2_next_id', qr0.id as 'qr0_id', qr0.next_id as 'qr0_next_id'
        from ( select min(q.id) 'id', q.next_id from (select e.id, (select id from events where id>e.id and locked=0 limit 1) as 'next_id'
        from events e where e.locked=2) q where next_id is not null group by q.next_id
        ) qr2 left join (select min(q.id) 'id', q.next_id from (select e.id, (select id from events where id>e.id and locked=2 limit 1) as 'next_id'
        from events e where e.locked=0) q where next_id is not null group by q.next_id having MINUTE(TIMEDIFF((select time from events where id=min(q.id) limit 1),(select time from events where id=q.next_id limit 1))) >= 10
        ) qr0 on qr2.next_id=qr0.id"

=end




