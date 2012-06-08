# represents one point with geo coordinates
class Location < ActiveRecord::Base
  include GeoHelper

  belongs_to :trip
  delegate :user, :to => :trip, :allow_nil => true
  alias_attribute :to_s, :address

  # try to geolocate the point
  after_save do |record|
    record.delay.set_address! if record.latitude_changed? or record.longitude_changed?
  end

  # checks if the location object inside of the allowed area
  def allowed?
    trip.restrictions.flatten.map {|r| r.contains?(self)}.reduce(:|)
  end

  # lat/lng or geolocated address
  def location
    address || "#{latitude} #{longitude}"
  end

  # json with only lat/lng data
  def to_js
    to_json(:only => [:latitude, :longitude]).html_safe
  end

  def set_address!
    update_attribute :address, self.geolocate
  end

  def api_point_detail
    {
      :latitude => self.latitude,
      :longitude => self.longitude,
      :address => self.address,
      :city => 'not defined',
      :zip => 'not defined',
      :country => 'not defined',
      :date => self.created_at.try(:to_date),
      :time => self.created_at.try(:to_time)
    }
  end

end

