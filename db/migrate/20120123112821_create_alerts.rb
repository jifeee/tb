class CreateAlerts < ActiveRecord::Migration
  def self.up
    add_column :restrictions, :alert_id, :integer
    remove_column :restrictions, :user_id

    create_table :alerts do |t|
      t.string :name
      t.references :phone
      t.enum :type
      t.datetime :restricted_time
      t.boolean :enabled, :default => true
      
      t.timestamps
    end
  end

  def self.down
    drop_table :alerts
    remove_column :restrictions, :alert_id
    add_column :restrictions, :user_id, :integer
  end
end
