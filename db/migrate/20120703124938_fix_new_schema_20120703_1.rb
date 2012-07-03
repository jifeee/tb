class FixNewSchema201207031 < ActiveRecord::Migration
  def self.up
  	add_index "phones_log", "imei", :unique => true unless index_exists?('phones_log', 'imei') 
  end

  def self.down
  end
end
