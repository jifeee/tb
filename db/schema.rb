# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20120727084354) do

  create_table "active_admin_comments", :force => true do |t|
    t.integer  "resource_id",   :null => false
    t.string   "resource_type", :null => false
    t.integer  "author_id"
    t.string   "author_type"
    t.text     "body"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "namespace"
  end

  add_index "active_admin_comments", ["author_type", "author_id"], :name => "index_active_admin_comments_on_author_type_and_author_id"
  add_index "active_admin_comments", ["namespace"], :name => "index_active_admin_comments_on_namespace"
  add_index "active_admin_comments", ["resource_type", "resource_id"], :name => "index_admin_notes_on_resource_type_and_resource_id"

  create_table "alert_histories", :force => true do |t|
    t.integer  "alert_id"
    t.integer  "trip_id"
    t.integer  "event_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "alert_histories", ["alert_id"], :name => "index_alert_histories_on_alert_id"
  add_index "alert_histories", ["trip_id", "alert_id"], :name => "index_alert_histories_on_trip_id_and_alert_id"
  add_index "alert_histories", ["trip_id"], :name => "index_alert_histories_on_trip_id"

  create_table "alert_trip_notifications", :force => true do |t|
    t.integer  "alert_id"
    t.integer  "trip_id"
    t.integer  "counter",    :default => 1
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "alert_trip_notifications", ["alert_id"], :name => "index_alert_trip_notifications_on_alert_id"
  add_index "alert_trip_notifications", ["trip_id", "alert_id"], :name => "index_alert_trip_notifications_on_trip_id_and_alert_id"
  add_index "alert_trip_notifications", ["trip_id"], :name => "index_alert_trip_notifications_on_trip_id"

  create_table "alerts", :force => true do |t|
    t.string   "name"
    t.string   "event_type"
    t.boolean  "enabled",               :default => true
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "restricted_time_start"
    t.string   "restricted_time_end"
    t.integer  "author_id"
    t.boolean  "is_repeat",             :default => true
    t.integer  "repeat_freq",           :default => 15
    t.integer  "repeat_count",          :default => 3
    t.integer  "speed"
    t.integer  "speed_over"
  end

  add_index "alerts", ["author_id"], :name => "index_alerts_on_author_id"
  add_index "alerts", ["enabled"], :name => "index_alerts_on_enabled"
  add_index "alerts", ["event_type"], :name => "index_alerts_on_event_type"

  create_table "alerts_phones", :id => false, :force => true do |t|
    t.integer "alert_id"
    t.integer "phone_id"
  end

  create_table "alerts_users", :id => false, :force => true do |t|
    t.integer "alert_id"
    t.integer "user_id"
  end

  create_table "calculated_events", :force => true do |t|
    t.integer  "last_event_id"
    t.string   "textbuster_mac", :limit => 17
    t.integer  "phones_log_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.datetime "last_time"
  end

  create_table "click", :force => true do |t|
    t.integer "x"
    t.integer "y"
  end

  create_table "delayed_jobs", :force => true do |t|
    t.integer  "priority",   :default => 0
    t.integer  "attempts",   :default => 0
    t.text     "handler"
    t.text     "last_error"
    t.datetime "run_at"
    t.datetime "locked_at"
    t.datetime "failed_at"
    t.string   "locked_by"
    t.string   "queue"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "delayed_jobs", ["priority", "run_at"], :name => "delayed_jobs_priority"

  create_table "devices", :force => true do |t|
    t.string   "imei",      :limit => 17
    t.string   "name"
    t.integer  "family_id"
    t.datetime "created"
    t.datetime "seen"
  end

  add_index "devices", ["imei"], :name => "index_devices_on_imei", :unique => true

  create_table "devices_phones", :id => false, :force => true do |t|
    t.integer "device_id"
    t.integer "phone_id"
  end

  create_table "events", :force => true do |t|
    t.integer  "phones_log_id",                               :null => false
    t.datetime "time",                                        :null => false
    t.integer  "type",           :limit => 8,  :default => 0, :null => false
    t.string   "screen",         :limit => 3
    t.string   "bluetooth",      :limit => 3
    t.string   "gps",            :limit => 4
    t.string   "locked",         :limit => 3
    t.string   "alert",          :limit => 3
    t.string   "textbuster_mac", :limit => 17
    t.integer  "locations_id"
    t.datetime "created"
  end

  add_index "events", ["locations_id"], :name => "index_events_on_locations_id"
  add_index "events", ["locked"], :name => "index_events_on_locked"
  add_index "events", ["phones_log_id"], :name => "index_events_on_phones_log_id"
  add_index "events", ["textbuster_mac", "phones_log_id"], :name => "index_events_on_textbuster_mac_and_phones_log_id"
  add_index "events", ["textbuster_mac"], :name => "index_events_on_textbuster_mac"
  add_index "events", ["time"], :name => "new_index"

  create_table "families", :force => true do |t|
  end

  create_table "locations", :force => true do |t|
    t.decimal  "lat",                     :precision => 10, :scale => 6
    t.decimal  "lng",                     :precision => 10, :scale => 6
    t.string   "address"
    t.integer  "trip_id"
    t.datetime "created_at"
    t.string   "country"
    t.string   "city"
    t.string   "zip"
    t.decimal  "alt",                     :precision => 10, :scale => 6
    t.integer  "spd"
    t.decimal  "bear",                    :precision => 10, :scale => 6
    t.integer  "time",       :limit => 8
    t.float    "acc"
  end

  add_index "locations", ["created_at"], :name => "index_locations_on_created_at"
  add_index "locations", ["trip_id"], :name => "index_locations_on_trip_id"

  create_table "menu_items", :force => true do |t|
    t.string   "resource"
    t.string   "menu_item"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "pages", :force => true do |t|
    t.string  "title"
    t.string  "anchor"
    t.text    "data"
    t.boolean "menu_logged_upt"
    t.boolean "menu_logged_in"
    t.boolean "menu_logged_out"
    t.boolean "menu_footer"
    t.boolean "menu_main",       :default => false
  end

  create_table "permissions", :force => true do |t|
    t.string "action"
    t.string "subject_class"
  end

  create_table "permissions_roles", :id => false, :force => true do |t|
    t.integer "permission_id"
    t.integer "role_id"
  end

  create_table "phones", :force => true do |t|
    t.string   "imei"
    t.integer  "user_id"
    t.string   "name"
    t.integer  "abilities"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "phones_log", :force => true do |t|
    t.string   "imei",    :limit => 65, :null => false
    t.datetime "created",               :null => false
    t.datetime "seen",                  :null => false
  end

  add_index "phones_log", ["imei"], :name => "imei_UNIQUE", :unique => true
  add_index "phones_log", ["imei"], :name => "index_phones_log_on_imei", :unique => true

  create_table "reports", :force => true do |t|
    t.string   "imei"
    t.integer  "event_id"
    t.integer  "conditions"
    t.text     "data"
    t.datetime "created_at"
    t.integer  "device_id"
  end

  create_table "restrictions", :force => true do |t|
    t.decimal  "latitude",                  :precision => 10, :scale => 6
    t.decimal  "longitude",                 :precision => 10, :scale => 6
    t.decimal  "radius",                    :precision => 5,  :scale => 1
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "alert_id"
    t.string   "address",    :limit => 256
  end

  create_table "roles", :force => true do |t|
    t.string "name"
  end

  create_table "sessions", :force => true do |t|
    t.string   "session_id", :null => false
    t.text     "data"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "sessions", ["session_id"], :name => "index_sessions_on_session_id"
  add_index "sessions", ["updated_at"], :name => "index_sessions_on_updated_at"

  create_table "settings", :force => true do |t|
    t.string "name"
    t.string "value"
  end

  create_table "trips", :force => true do |t|
    t.decimal  "distance",        :precision => 8, :scale => 2
    t.decimal  "average_speed",   :precision => 5, :scale => 2
    t.integer  "user_id"
    t.integer  "device_id"
    t.decimal  "timezone",        :precision => 2, :scale => 1
    t.integer  "start_point_id"
    t.integer  "end_point_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer  "phone_id"
    t.datetime "last_time_event"
  end

  create_table "users", :force => true do |t|
    t.string   "email",                                 :default => "",    :null => false
    t.string   "encrypted_password",     :limit => 128, :default => "",    :null => false
    t.string   "reset_password_token"
    t.datetime "reset_password_sent_at"
    t.integer  "sign_in_count",                         :default => 0
    t.datetime "current_sign_in_at"
    t.datetime "last_sign_in_at"
    t.string   "current_sign_in_ip"
    t.string   "last_sign_in_ip"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "name"
    t.string   "login"
    t.integer  "role_id"
    t.integer  "family_id"
    t.string   "authentication_token"
    t.text     "phone"
    t.text     "aux_emails"
    t.datetime "remember_created_at"
    t.boolean  "is_main",                               :default => false, :null => false
  end

  add_index "users", ["email"], :name => "index_users_on_email", :unique => true
  add_index "users", ["family_id", "role_id"], :name => "index_users_on_family_id_and_role_id"
  add_index "users", ["family_id"], :name => "index_users_on_family_id"
  add_index "users", ["reset_password_token"], :name => "index_users_on_reset_password_token", :unique => true

end
