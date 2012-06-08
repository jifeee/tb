class CreateRestrictions < ActiveRecord::Migration
  def self.up
    create_table :restrictions do |t|
      t.references :user
      
      t.decimal :latitude,  :precision => 9, :scale => 6
      t.decimal :longitude, :precision => 9, :scale => 6
      
      t.decimal :radius, :precision => 5, :scale => 1
      t.timestamps
    end
  end

  def self.down
    drop_table :restrictions
  end
end
