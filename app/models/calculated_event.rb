class CalculatedEvent < ActiveRecord::Base
	belongs_to :phones_log, :foreign_key => 'phones_log_id'
	belongs_to :device, :foreign_key => 'textbuster_mac', :primary_key => 'imei'
end
