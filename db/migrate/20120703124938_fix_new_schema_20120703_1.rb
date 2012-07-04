class FixNewSchema201207031 < ActiveRecord::Migration
  def self.up
  	add_index "phones_log", "imei", :unique => true rescue nil
  end

  def self.down
  end
end
