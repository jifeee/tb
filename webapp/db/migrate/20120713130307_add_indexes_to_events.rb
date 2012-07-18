class AddIndexesToEvents < ActiveRecord::Migration
  def self.up
		add_index "events", "textbuster_mac"
		add_index "events", "locked"
		add_index "events", ["textbuster_mac","phones_log_id"]
  end

  def self.down
  	remove_index "events", "textbuster_mac"
		remove_index "events", "locked"
		remove_index "events", ["textbuster_mac","phones_log_id"]
  end
end
