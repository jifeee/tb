class AddMainMenuToPages < ActiveRecord::Migration
  def self.up
  	add_column 'pages', 'menu_main', :boolean, :default => 0
  end

  def self.down
  	remove_column 'pages', 'menu_main'
  end
end
