class AddAlertMinSpeed < ActiveRecord::Migration
  def self.up
  	Setting.create :name => 'alert_min_speed', :value => 5
  end

  def self.down
  	Setting.find_by_name('alert_min_speed').destroy
  end
end
