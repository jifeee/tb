class AddAddressToRestriction < ActiveRecord::Migration
  def self.up
    add_column :restrictions, :address, :string
  end

  def self.down
    remove_column :restrictions, :address
  end
end
