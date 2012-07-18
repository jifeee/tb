class CreateMenuItems < ActiveRecord::Migration
  def self.up
    create_table :menu_items do |t|
      t.string :resource
      t.string :menu_item
      
      t.timestamps
    end
    MenuItem.reset_column_information
    
    MenuItem.create :resource => 'Phone', :menu_item => 'Phones'
    MenuItem.create :resource => 'Device', :menu_item => 'Textbusters'
    MenuItem.create :resource => 'Event', :menu_item => 'Events'
    MenuItem.create :resource => 'Trip', :menu_item => 'Trips'
    MenuItem.create :resource => 'Alert', :menu_item => 'Alerts'
    MenuItem.create :resource => 'Family', :menu_item => 'Parents'
  end

  def self.down
    drop_table :menu_items
  end
end
