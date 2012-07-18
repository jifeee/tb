class AddIndexesToAlertTripNotifications < ActiveRecord::Migration
	def self.up
  	add_index "alert_trip_notifications", "trip_id"
  	add_index "alert_trip_notifications", "alert_id"
  	add_index "alert_trip_notifications", ["trip_id","alert_id"]  	
  end

  def self.down
  	remove_index "alert_trip_notifications", "trip_id"
  	remove_index "alert_trip_notifications", "alert_id"
  	remove_index "alert_trip_notifications", ["trip_id","alert_id"]  	
  end
end
