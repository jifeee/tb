class CreateAlertHistories < ActiveRecord::Migration
  def self.up
    create_table :alert_histories do |t|
    	t.belongs_to :alert
    	t.belongs_to :trip
    	t.belongs_to :event

      t.timestamps
    end
  end

  def self.down
    drop_table :alert_histories
  end
end
