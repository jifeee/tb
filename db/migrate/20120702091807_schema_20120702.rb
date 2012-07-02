class Schema20120702 < ActiveRecord::Migration
  def self.up
  	#  Devices
  	rename_column 'devices', 'iddevices', 'id'
  	add_index "devices", ["imei"]

	#  Events
	rename_column 'events', 'idevents', 'id'
	rename_column 'events', 'device', 'devices_id'
	add_column 'events', 'type', :integer, :limit => 2, :default => 0, :null => false
	add_column 'events', 'locked', :string
	add_column 'events', 'alert', :string
	rename_column 'events', 'location', 'locations_id'
	add_column 'events', 'click_id', :integer
	add_column 'events', 'created', :datetime
	remove_column 'events', "alt"
	remove_column 'events', "lat"
	remove_column 'events', "lon"
	remove_column 'events', "speed"
	remove_column 'events', 'latitude'
	remove_column 'events', 'longitude'
	remove_column 'events', 'conditions'
	add_index "events", ["click_id"]
	add_index "events", ["devices_id"]
	add_index "events", ["locations_id"]

	#  Locations
	add_column 'locations', 'acc', :float

  end

  def self.down
  	#  Devices
  	remove_index "devices", :column => 'imei'
  	rename_column 'devices', 'id', 'iddevices'

  	#  Events
 	remove_index "events", :column => "click_id"
	remove_index "events", :column => "devices_id"
	remove_index "events", :column => "locations_id"
  	rename_column 'events', 'id', 'idevents'
  	rename_column 'events', 'devices_id', 'device'
  	remove_column 'events', 'type'
  	remove_column 'events', 'locked'
  	remove_column 'events', 'alert'
  	rename_column 'events', 'locations_id', 'location'
  	remove_column 'events', 'click_id'
	remove_column 'events', 'created'
	add_column 'events', 'alt', :text
	add_column 'events', 'lat', :string
	add_column 'events', 'lon', :string
	add_column 'events', "speed", :decimal, :precision => 8, :scale => 2
	add_column 'events', "latitude", :decimal, :precision => 9, :scale => 6
    add_column 'events', "longitude", :decimal, :precision => 9, :scale => 6
    add_column 'events', 'conditions', :integer

	#  Locations
	remove_column 'locations', 'acc'

  end
end
