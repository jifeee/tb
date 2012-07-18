class AddLastTimeToCalculatedEventscalculatedEvents < ActiveRecord::Migration
  def self.up
  	add_column 'calculated_events', 'last_time', :datetime
  end

  def self.down
  	remove_column 'calculated_events', 'last_time'
  end
end
