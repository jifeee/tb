class AddAlertSpeedOver < ActiveRecord::Migration
  def self.up
  	add_column 'alerts', 'speed_over', :integer
  	Setting.create :name => 'speed_over', :value => '15,20'
  end

  def self.down
  	remove_column 'alerts', 'speed_over'
  	Setting.where(:name => 'speed_over').delete_all
  end
end
