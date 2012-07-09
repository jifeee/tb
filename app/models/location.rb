# represents one point with geo coordinates
class Location < ActiveRecord::Base
  include GeoHelper

  attr_accessor :geo

  belongs_to :trip

  has_many :events, :foreign_key => 'locations_id'

  delegate :user, :to => :trip, :allow_nil => true
  alias_attribute :to_s, :address

  alias_attribute :longitude, :lng
  alias_attribute :latitude, :lat

  # try to geolocate the point
  after_save do |record|
    record.delay.set_address! if record.lat_changed? or record.lng_changed?
  end

  #  Redefine attributes read
  %w(country city zip address).map do |attr|
    define_method("#{attr}") do
      get_attr_geo(attr.to_sym)    
    end
  end

  # checks if the location object inside of the allowed area
  def allowed?
    trip.restrictions.flatten.map {|r| r.contains?(self)}.reduce(:|)
  end

  def restricted_zones
    trip.restrictions.flatten.select {|r| r.contains?(self) == false}
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

