class CreateSettings < ActiveRecord::Migration
  def self.up
    create_table :settings do |t|
      t.string :name
      t.string :value
    end    
  end

  def self.down
    drop_table :settings
  end
end
