class AlterLocationTime < ActiveRecord::Migration
  def self.up
  	change_column 'locations','time', :integer, :limit => 8
  end

  def self.down
  end
end
