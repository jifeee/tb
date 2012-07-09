class AddIsMainToUsers < ActiveRecord::Migration
  def self.up
  	add_column 'users', 'is_main', :boolean, :null => false, :default => false
  end

  def self.down
  	remove_column 'users', 'is_main'
  end
end
