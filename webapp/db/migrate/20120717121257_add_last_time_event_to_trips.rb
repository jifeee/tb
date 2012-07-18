class AddLastTimeEventToTrips < ActiveRecord::Migration
  def self.up
  	add_column 'trips', 'last_time_event', :datetime
  end

  def self.down
  	remove_column 'trips', 'last_time_event'
  end
end
