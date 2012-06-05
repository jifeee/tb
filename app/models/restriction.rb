class Restriction < ActiveRecord::Base
  include GeoHelper
  belongs_to :alert
  
  # checks if location point is inside of allowed area
  def contains? loc
    loc.distance_to(self) <= radius
  end
  
  def location
    address || "lat: #{latitude}, lng: #{longitude}"
  end
  
  def to_s
    "#{radius} km away from #{location}"
  end
end
