class AddTimeRestrictions < ActiveRecord::Migration
  def self.up
    remove_column :alerts, :restricted_time
    add_column :alerts, :restricted_time_start, :string
    add_column :alerts, :restricted_time_end, :string
  end

  def self.down
    remove_column :alerts, :restricted_time_start
    remove_column :alerts, :restricted_time_end
    add_column :alerts, :restricted_time, :time
  end
end
