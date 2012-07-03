class CreateClick < ActiveRecord::Migration
  def self.up
		create_table "click", :force => true do |t|
			t.integer "x"
			t.integer "y"
		end
  end

  def self.down
  	drop_table 'click'
  end
end
