class Schema201207032 < ActiveRecord::Migration
  def self.up
  	drop_table :events

  	 create_table "events", :force => true do |t|
	    t.integer  "phones_log_id", :null => false
	    t.datetime "time", :null => false
	    t.integer  "type", :limit => 5, :default => 0, :null => false
	    t.string   "screen", :limit => 3
	    t.string   "bluetooth", :limit => 3
	    t.string   "gps", :limit => 4
	    t.string   "locked", :limit => 3
	    t.string   "alert", :limit => 3
	    t.string   "textbuster_mac", :limit => 17
	    t.integer  "locations_id"
	    t.datetime "created"
	  end  	
  end

  def self.down
  	drop_table :events

  	create_table "events", :force => true do |t|
	    t.string   "event_type"
	    t.integer  "device_id",                                :null => false
	    t.integer  "user_id"
	    t.integer  "phone_id"
	    t.datetime "created_at"
	    t.integer  "report_id"
	    t.datetime "time",                                     :null => false
	    t.string   "screen",       :limit => 3
	    t.string   "bluetooth",    :limit => 3
	    t.string   "gps",          :limit => 4
	    t.integer  "locations_id"
	    t.integer  "type",         :limit => 2, :default => 0, :null => false
	    t.string   "locked",       :limit => 3
	    t.string   "alert",        :limit => 3
	    t.integer  "click_id"
	    t.datetime "created"
	  end  	
  end
end
