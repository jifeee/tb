class AlertHistory < ActiveRecord::Base

	belongs_to :alert
	belongs_to :trip
	belongs_to :event

	def short_description
		if self.alert.event_type == :'Leaving area'
			"violation of zone"
		elsif self.alert.event_type == :"Driving at a specific time"
			"violation of time"
		elsif self.alert.event_type == :"Speed restriction"
			"violation of speed"
		end
	end

end
