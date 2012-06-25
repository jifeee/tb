# represents one point with geo coordinates
class Location < ActiveRecord::Base
  self.primary_key = "idlocations"

  include GeoHelper

  attr_accessor :geo

  belongs_to :trip
  delegate :user, :to => :trip, :allow_nil => true
  alias_attribute :to_s, :address

  # try to geolocate the point
  after_save do |record|
    record.delay.set_address! if record.lat_changed? or record.lng_changed?
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

  def country
    if self['country'].nil?
      self.update_attribute(:country, geo[:country])
    end
    self['country']
  end

  def city
    if self['city'].nil?
      self.update_attribute(:city, geo[:city]) 
    end
    self['city']
  end

  def zip
    if self['zip'].nil?
      self.update_attribute(:zip, geo[:zip]) 
    end
    self['zip']
  end

  def geo
    if @geo.nil? || @geo.blank?
      puts 'Getting geo locations ....'
      @geo = {}
      response = RestClient.get 'maps.googleapis.com/maps/api/geocode/json', {:params => {:latlng => "#{self.lat},#{self.lng}", :sensor => false}}
      r = JSON.parse(response)['results']
      r.map do |a|
        @geo[:street] = a['formatted_address'] if @geo[:street].blank? || @geo[:street].size < a['formatted_address'].size
        a['address_components'].map do |aa|
          @geo[:country] = aa['long_name'] if aa['types'] == ["country", "political"]
          @geo[:city] = aa['long_name'] if aa['types'] == ["locality", "political"]
          @geo[:zip] = aa['long_name'] if aa['types'] == ["postal_code"]
        end
      end
    end
    p @geo
    return @geo
  end

end

