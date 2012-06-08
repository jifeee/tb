class CreateDevices < ActiveRecord::Migration
  def self.up
    create_table :devices do |t|
      t.references :user
      t.string :imei
      t.string :mac
    end
  end

  def self.down
    drop_table :devices
  end
end
