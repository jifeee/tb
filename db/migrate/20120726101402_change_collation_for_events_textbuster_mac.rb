class ChangeCollationForEventsTextbusterMac < ActiveRecord::Migration
  def self.up
  	# change_column 'events', 'textbuster_mac', :string, :limit => 17, :collate => 'utf8_general_ci'
  	execute "ALTER TABLE `devices` CHANGE COLUMN `imei` `imei` VARCHAR(17) CHARACTER SET 'utf8' COLLATE 'utf8_general_ci' NULL DEFAULT NULL;"
  	execute "ALTER TABLE `events` CHANGE COLUMN `textbuster_mac` `textbuster_mac` VARCHAR(17) CHARACTER SET 'utf8' COLLATE 'utf8_general_ci' NULL DEFAULT NULL;"
  end

  def self.down
  end
end
