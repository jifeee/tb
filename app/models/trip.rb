# Represents a trip as collection of lat/lng points
class Trip < ActiveRecord::Base
  belongs_to :user
  belongs_to :device
  belongs_to :phone
  
  has_many :locations, :order => :created_at
  belongs_to :start_point, :class_name => 'Location', :foreign_key => :start_point_id
  belongs_to :end_point, :class_name => 'Location', :foreign_key => :end_point_id
  
  delegate :restrictions, :to => :phone, :allow_nil => true
  
  # get timezone form lat/lng for the trip
  after_create do |record|
    record.update_attribute :timezone, record.start_point.timeshift
  end
  
  # show the most recent point if trip not finished
  alias :finish :end_point
  def end_point
    finish || locations.last
  end
  
  # location points excluding start and end
  def stopps
    locations - [start_point, end_point]
  end
  
  # short info as hash representation for API
  def to_hash
    {:trip_id => id,
     :distance => distance,
     :average_speed => average_speed,
     :start => start_point.location,
     :start_time => start_point.created_at.to_i,
     :end => end_point.location,
     :end_time => end_point.created_at.to_i}.merge(stopps.any? ? {:points => stopps.map(&:location)} : {})
  end
  
  # check if all the trip points meet restrictions
  def allowed?
    locations.map(&:allowed?).reduce(:|)
  end
  
  def status
    allowed? ? "on" : "alert" rescue 'on'
  end 

  def self.create_trips
    self.connection.execute(sanitize_sql("
      select qr2.*, qr0.*
from
(
select 
 min(q.id) 'id',
 q.next_id
from (select e.id, (select id from events where id>e.id and locked=0 limit 1) as 'next_id'
from events e where e.locked=2) q
where next_id is not null
group by q.next_id
) qr2
left join 
(
select 
 min(q.id) 'id',
 q.next_id
from (select e.id, (select id from events where id>e.id and locked=2 limit 1) as 'next_id'
from events e where e.locked=0) q
where next_id is not null
group by q.next_id
having 
 MINUTE(TIMEDIFF((select time from events where id=min(q.id) limit 1),(select time from events where id=q.next_id limit 1))) >= 10
) qr0 on qr2.next_id=qr0.id"
      ))
  end
  
end
