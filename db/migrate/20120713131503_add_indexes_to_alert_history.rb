class AddIndexesToAlertHistory < ActiveRecord::Migration
	def self.up
  	add_index "alert_histories", "trip_id"
  	add_index "alert_histories", "alert_id"
  	add_index "alert_histories", ["trip_id","alert_id"]  	
  end

  def self.down
  	remove_index "alert_histories", "trip_id"
  	remove_index "alert_histories", "alert_id"
  	remove_index "alert_histories", ["trip_id","alert_id"]  	
  end
end
