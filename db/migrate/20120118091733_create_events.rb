class CreateEvents < ActiveRecord::Migration
  def self.up
    create_table :events do |t|
      t.string :event_type
      t.integer :conditions
      
      t.decimal :latitude, :precision => 9, :scale => 6
      t.decimal :longitude, :precision => 9, :scale => 6
      t.decimal :speed, :precision => 8, :scale => 2
      
      t.references :device
      t.references :user
      t.references :phone
      
      t.timestamp :created_at
    end
  end

  def self.down
    drop_table :events
  end
end
