class FixNewSchema02 < ActiveRecord::Migration
  def self.up
  	remove_index "devices", :column => 'imei' if index_exists?('devices', 'imei') 
  	add_index "devices", "imei", :unique => true 
  end

  def self.down
  end
end
