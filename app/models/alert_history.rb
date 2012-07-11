class AlertHistory < ActiveRecord::Base

	belongs_to :alert
	belongs_to :trip
	belongs_to :event

	def short_description
		if self.alert.event_type == 'Leaving area'.to_sym
			"violation of zone"
		elsif
			"violation of time"
		end
	end

end
