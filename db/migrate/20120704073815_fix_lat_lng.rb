class FixLatLng < ActiveRecord::Migration
  def self.up
  	change_column 'locations', 'lat', :decimal, :precision => 10, :scale => 6
  	change_column 'locations', 'lng', :decimal, :precision => 10, :scale => 6
  	change_column 'locations', 'bear', :decimal, :precision => 10, :scale => 6
  	change_column 'locations', 'alt', :decimal, :precision => 10, :scale => 6

  	change_column 'restrictions', 'latitude', :decimal, :precision => 10, :scale => 6
  	change_column 'restrictions', 'longitude', :decimal, :precision => 10, :scale => 6  	
  end

  def self.down
  end
end
