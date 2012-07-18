class CreatePermissions < ActiveRecord::Migration
  def self.up
    create_table :permissions do |t|
      t.string :action
      t.string :subject_class
    end
    
    create_table :permissions_roles, :id => false do |t|
      t.references :permission
      t.references :role
    end
    
    remove_column :roles, :permissions
  end

  def self.down
    drop_table :permissions
    drop_table :permissions_roles
    
    add_column :roles, :permissions, :string
  end
end
