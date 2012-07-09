class CreateAlertTripNotifications < ActiveRecord::Migration
  def self.up
    create_table :alert_trip_notifications do |t|
    	t.belongs_to :alert
    	t.belongs_to :trip
    	t.integer :counter, :default => 1
      t.timestamps
    end
  end

  def self.down
    drop_table :alert_trip_notifications
  end
end
