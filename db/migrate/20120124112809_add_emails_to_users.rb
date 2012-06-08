class AddEmailsToUsers < ActiveRecord::Migration
  def self.up
    add_column :users, :aux_emails, :text
    change_column :users, :phone, :text
  end

  def self.down
    remove_column :users, :aux_emails
    change_column :users, :phone, :string
  end
end
