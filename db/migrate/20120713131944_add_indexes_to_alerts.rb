class AddIndexesToAlerts < ActiveRecord::Migration
	def self.up
  	add_index "alerts", "event_type"
  	add_index "alerts", "enabled"
  	add_index "alerts", "author_id"
  end

  def self.down
  	remove_index "alerts", "event_type"
  	remove_index "alerts", "enabled"
  	remove_index "alerts", "author_id"
  end
end
