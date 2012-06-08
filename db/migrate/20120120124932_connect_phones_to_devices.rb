class ConnectPhonesToDevices < ActiveRecord::Migration
  def self.up
    create_table :devices_phones, :id => false do |t|
      t.references :device
      t.references :phone
    end
  end

  def self.down
    drop_table :devices_phones
  end
end
