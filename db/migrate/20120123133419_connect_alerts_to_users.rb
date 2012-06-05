class ConnectAlertsToUsers < ActiveRecord::Migration
  def self.up
    create_table :alerts_users, :id => false do |t|
      t.references :alert
      t.references :user
    end
  end

  def self.down
    drop_table :alerts_users
  end
end
