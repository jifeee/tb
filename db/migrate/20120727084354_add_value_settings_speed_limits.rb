class AddValueSettingsSpeedLimits < ActiveRecord::Migration
  def self.up
  	Setting.create :name => 'speed_limit', :value => '25,35,45,55,65,75'
  end

  def self.down
  	Setting.where(:name => 'speed_limit').delete_all
  end
end
