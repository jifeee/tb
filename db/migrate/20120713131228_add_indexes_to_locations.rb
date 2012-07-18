class AddIndexesToLocations < ActiveRecord::Migration
  def self.up
  	add_index "locations", "trip_id"
  end

  def self.down
  	remove_index "locations", "trip_id"
  end
end
