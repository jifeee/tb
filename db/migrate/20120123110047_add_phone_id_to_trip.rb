class AddPhoneIdToTrip < ActiveRecord::Migration
  def self.up
    add_column :trips, :phone_id, :integer
  end

  def self.down
    remove_column :trips, :phone_id
  end
end
