class AddAdmin_emailToSettings < ActiveRecord::Migration
  def self.up
	Setting.create :name => 'admin_email', :value => 'admin@textbuster.com'
  end

  def self.down
  	Setting.where(:name => 'admin_email').delete_all
  end
end