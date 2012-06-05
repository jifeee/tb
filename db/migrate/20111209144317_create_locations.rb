class CreateLocations < ActiveRecord::Migration
  def self.up
    create_table :locations do |t|
      t.decimal :latitude,  :precision => 8, :scale => 6
      t.decimal :longitude, :precision => 8, :scale => 6
      t.string :address
      
      t.references :trip
      t.timestamp :created_at
    end
    
    add_index :locations, :created_at
  end

  def self.down
    drop_table :locations
  end
end
