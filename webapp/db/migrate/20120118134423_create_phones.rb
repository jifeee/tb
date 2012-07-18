class CreatePhones < ActiveRecord::Migration
  def self.up
    create_table :phones do |t|
      t.string :imei
      t.references :user
      t.string :name
      t.integer :abilities
      
      t.timestamps
    end
    
    add_column :devices, :name, :string
    remove_column :devices, :imei
    remove_column :devices, :user_id
  end

  def self.down
    drop_table :phones
    
    add_column :devices, :imei, :string
    add_column :devices, :user_id, :integer
    remove_column :devices, :name
  end
end
