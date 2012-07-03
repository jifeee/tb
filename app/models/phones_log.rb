class PhonesLog < ActiveRecord::Base
	set_table_name :phones_log

	belongs_to :phone, :foreign_key => 'imei', :primary_key => 'imei'
	
	has_many :events
end