class AddSpeedToAlert < ActiveRecord::Migration
  def self.up
  	add_column 'alerts','speed', :integer
  end

  def self.down
  	remove_column 'alerts','speed'
  end
end
