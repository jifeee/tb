class CreateReports < ActiveRecord::Migration
  def self.up
    create_table :reports do |t|
      t.string :imei    # probably change to phone_id
      t.references :event
      t.integer :conditions
      t.text :data
      
      t.timestamp :created_at
    end
  end

  def self.down
    drop_table :reports
  end
end
