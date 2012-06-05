class CreateTrips < ActiveRecord::Migration
  def self.up
    create_table :trips do |t|
      t.integer :distance
      t.decimal :average_speed, :precision => 5, :scale => 2
      t.decimal :timezone, :precision => 3, :scale => 1
      t.references :user
      t.references :device
      t.references :start_point
      t.references :end_point
      
      t.timestamps
    end
  end

  def self.down
    drop_table :trips
  end
end
