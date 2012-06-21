class FixDevidcesFamilyId < ActiveRecord::Migration
  def self.up
  	unless column_exists? :devices, :family_id
  		add_column 'devices', 'family_id', :integer
	end
  end

  def self.down
  	remove_column 'devices', 'family_id'
  end
end
