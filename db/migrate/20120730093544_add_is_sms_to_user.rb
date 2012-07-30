class AddIsSmsToUser < ActiveRecord::Migration
  def self.up
  	add_column 'users', 'is_sms', :boolean, :default => false, :null => false
  	add_column 'users', 'is_allowed_sms', :boolean, :default => false, :null => false
  end

  def self.down
  	remove_column 'users', 'is_sms'
  	remove_column 'users', 'is_allowed_sms'
  end
end
