class CreateFamilies < ActiveRecord::Migration
  def self.up
    create_table :families
  end

  def self.down
    drop_table :families
  end
end
