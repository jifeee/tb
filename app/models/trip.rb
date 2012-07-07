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
    record.update_attribute :timezone, record.start_point.timeshift rescue nil
  end
  
  # show the most recent point if trip not finished
  alias :finish :end_point
  def end_point
    finish || locations.last
  end
  
  # location points excluding start and end
  def stopps(waypoins = nil)
    locations_array = locations - [start_point, end_point]
    if waypoins && locations_array.size >= 8
      z = (locations_array.size.to_f / waypoins).ceil rescue 1
      locations_array = locations_array.in_groups_of(z).map {|a| a.first}
    end
    locations_array
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
  
end
