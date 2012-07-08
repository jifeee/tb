class AddAlertFrqCnt < ActiveRecord::Migration
  def self.up
  	add_column 'alerts', 'is_repeat', :boolean, :default => true
  	add_column 'alerts', 'repeat_freq', :integer, :default => 15
  	add_column 'alerts', 'repeat_count', :integer, :default => 3
  end

  def self.down
  	remove_columns 'alerts', 'is_repeat', 'repeat_freq', 'repeat_count'
  end
end
