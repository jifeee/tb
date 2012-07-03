class Schema20120703 < ActiveRecord::Migration
  def self.up
  	rename_column 'events', 'devices_id', 'device_id'
  end

  def self.down
  	rename_column 'events', 'device_id', 'devices_id'
  end
end
