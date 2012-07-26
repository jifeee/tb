# represents one point with geo coordinates
class Location < ActiveRecord::Base
  include GeoHelper

  attr_accessor :geo

  belongs_to :trip

  has_many :events, :foreign_key => 'locations_id', :dependent => :destroy

  delegate :user, :to => :trip, :allow_nil => true
  alias_attribute :to_s, :address

  alias_attribute :longitude, :lng
  alias_attribute :latitude, :lat

  # try to geolocate the point
  after_save :update_address

  #  Redefine attributes read
  %w(country city zip address).map do |attr|
    define_method("#{attr}") do
      get_attr_geo(attr.to_sym)    
    end
  end

  def update_address
    self.delay.set_address! if self.lat_changed? or self.lng_changed?    
  end

  # checks if the location object inside of the allowed area
  def allowed?
    trip.restrictions.flatten.map {|r| r.contains?(self)}.reduce(:|)
  end

  def restricted_zones
    z = trip.restrictions.flatten.select {|r| r.contains?(self) == false}
    z.any? ? z : nil
  end

  # lat/lng or geolocated address
  def location
    address || "#{latitude} #{longitude}"
  end

  # json with only lat/lng data
  def to_js
    {:longitude => lng, :latitude => lat}.to_json.html_safe
    
  end

  def set_address!
    update_attribute :address, self.geolocate
  end

  def api_point_detail
    {
      :latitude => self.lat,
      :longitude => self.lng,
      :address => self.address,
      :city => self.city,
      :zip => self.zip,
      :country => self.country,
      :date => self.created_at.try(:to_date),
      :time => self.created_at.try(:to_time)
    }
  end

  def created_at
    Time.at(self['time']) if self['created_at'].blank?
  end

  def time
    Time.at(self['time'].to_i/1000) rescue nil
  end

  def time_with_timezone(timezone = nil)
    timezone = trip.timezone unless timezone
    # time.in_time_zone(timezone) rescue time
    time + timezone.to_i.hours rescue time
  end

protected

  def get_attr_geo(attr)
    self.update_attribute(attr, geo[attr]) if self[attr].blank?
    self[attr]
  end

  def geo
    # if @geo.nil? || @geo.blank?
    puts 'Getting geo locations ....'
    @geo = {}
    response = RestClient.get 'maps.googleapis.com/maps/api/geocode/json', {:params => {:latlng => "#{self.lat},#{self.lng}", :sensor => false}}
    r = JSON.parse(response)['results']
    r.map do |a|
      @geo[:address] = a['formatted_address'] if @geo[:address].blank? || @geo[:address].size < a['formatted_address'].size
      a['address_components'].map do |aa|
        @geo[:country] = aa['long_name'] if aa['types'] == ["country", "political"]
        @geo[:city] = aa['long_name'] if aa['types'] == ["locality", "political"]
        @geo[:zip] = aa['long_name'] if aa['types'] == ["postal_code"]
      end
    end
    p @geo
    return @geo
  end

end

