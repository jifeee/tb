class FixNewSchema01 < ActiveRecord::Migration
  def self.up
  	rename_column 'locations', 'idlocations', 'id'
  	execute "ALTER TABLE devices CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT;"
  	execute "ALTER TABLE events CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT;"
  	execute "ALTER TABLE locations CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT;"
  end

  def self.down
  	rename_column 'locations', 'id', 'idlocations'
  end
end
