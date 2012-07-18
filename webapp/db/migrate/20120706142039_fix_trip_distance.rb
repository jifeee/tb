class FixTripDistance < ActiveRecord::Migration
  def self.up
  	change_column 'trips', :distance, :decimal, :precision => 8, :scale => 2
  end

  def self.down
  end
end
