class AlertHabtmPhonesConnection < ActiveRecord::Migration
  def self.up
    create_table :alerts_phones, :id => false do |t|
      t.references :alert
      t.references :phone
    end
    remove_column :alerts, :phone_id
    rename_column :alerts, :type, :event_type
    add_column :alerts, :author_id, :integer
  end

  def self.down
    drop_table :alerts_phones
    add_column :alerts, :phone_id, :integer
    rename_column :alerts, :event_type, :type
    remove_column :alerts, :author_id
  end
end
