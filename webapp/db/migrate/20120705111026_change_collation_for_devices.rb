class ChangeCollationForDevices < ActiveRecord::Migration
  def self.up
  	change_column 'devices', 'imei', :string, :limit => 17, :collate => 'utf8_general_ci'
  	# ALTER TABLE `textbuster-dev`.`devices` CHANGE COLUMN `imei` `imei` VARCHAR(255) CHARACTER SET 'utf8' COLLATE 'utf8_general_ci' NULL DEFAULT NULL  ;
  end

  def self.down
  end
end
