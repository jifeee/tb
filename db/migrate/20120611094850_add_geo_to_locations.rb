class AddGeoToLocations < ActiveRecord::Migration
  def self.up
  	add_column 'locations', 'country', :string
  	add_column 'locations', 'city', :string
  	add_column 'locations', 'zip', :string
  end

  def self.down
  	remove_column 'locations', 'country'
  	remove_column 'locations', 'city'
  	remove_column 'locations', 'zip'
  end
end
