class CreateCalculatedEvents < ActiveRecord::Migration
  def self.up
    create_table :calculated_events do |t|
    	t.integer :last_event_id
    	t.string :textbuster_mac, :limit => 17
    	t.integer :phones_log_id

      t.timestamps
    end
  end

  def self.down
    drop_table :calculated_events
  end
end
