class AddIsDeletedToDevices < ActiveRecord::Migration
  def self.up
  	add_column 'devices', 'is_deleted', :boolean, :default => false, :null => false
  end

  def self.down
  	remove_column 'devices', 'is_deleted'
  end
end
