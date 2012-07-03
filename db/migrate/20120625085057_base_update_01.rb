class BaseUpdate01 < ActiveRecord::Migration
  def self.up
  	#  Devices
  	rename_column('devices', 'id', 'iddevices')
  	rename_column('devices', 'mac', 'imei')
  	add_column 'devices', 'created', :datetime
  	add_column 'devices', 'seen', :datetime
  	execute "ALTER TABLE devices CHANGE COLUMN `iddevices` `iddevices` INT(11) NOT NULL AUTO_INCREMENT;"

	#  Events
	rename_column('events', 'id', 'idevents')
	rename_column('events', 'device_id', 'device')
	add_column 'events', 'time', :datetime, :null => false
	add_column 'events', 'screen', :string, :limit => 12, :null => false
	add_column 'events', 'bluetooth', :string, :limit => 12, :null => false
	add_column 'events', 'gps', :string, :limit => 12, :null => false
	add_column 'events', 'location', :integer
	add_column 'events', 'alt', :text
	add_column 'events', 'lat', :string
	add_column 'events', 'lon', :string
	execute "ALTER TABLE events CHANGE COLUMN `idevents` `idevents` INT(11) NOT NULL AUTO_INCREMENT;"

	#  Locations
	rename_column('locations', 'id', 'idlocations')
	rename_column('locations', 'latitude', 'lat')
	rename_column('locations', 'longitude', 'lng')
	add_column 'locations', 'alt', :decimal, :precision => 8, :scale => 6
	add_column 'locations', 'spd', :decimal, :precision => 8, :scale => 6
	add_column 'locations', 'bear', :decimal, :precision => 8, :scale => 6
	add_column 'locations', 'time', :integer
	execute "ALTER TABLE locations CHANGE COLUMN `idlocations` `idlocations` INT(11) NOT NULL AUTO_INCREMENT;"
  end

  def self.down
  	#  Devices
  	rename_column('devices', 'iddevices', 'id')
  	rename_column('devices', 'imei', 'mac')
  	remove_column 'devices', 'created'
  	remove_column 'devices', 'seen'
  	execute "ALTER TABLE devices CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT;"

  	#  Events
  	rename_column('events', 'idevents', 'id')
	rename_column('events', 'device', 'device_id')
	remove_column 'events', 'time'
	remove_column 'events', 'screen'
	remove_column 'events', 'bluetooth'
	remove_column 'events', 'gps'
	remove_column 'events', 'location'
	remove_column 'events', 'alt'
	remove_column 'events', 'lat'
	remove_column 'events', 'lon'
	execute "ALTER TABLE events CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT;"

  	#  Locations
  	rename_column('locations', 'idlocations', 'id')
	rename_column('locations', 'lat', 'latitude')
	rename_column('locations', 'lng', 'longitude')
	remove_column 'locations', 'alt'
	remove_column 'locations', 'spd'
	remove_column 'locations', 'bear'
	remove_column 'locations', 'time'
	execute "ALTER TABLE locations CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT;"
  end
end
