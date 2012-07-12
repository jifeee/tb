class AlterLocationSpd < ActiveRecord::Migration
  def self.up
  	change_column 'locations', 'spd', :integer
  end

  def self.down
  end
end
