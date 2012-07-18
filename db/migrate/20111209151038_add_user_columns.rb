class AddUserColumns < ActiveRecord::Migration
  def self.up
    change_table :users do |t|
      t.string :name
      t.string :login
      t.references :role
      t.references :family
    end
    
    add_index :users, [:family_id, :role_id]
    add_index :users, :family_id
  end

  def self.down
    remove_column :users, :name, :login
  end
end
