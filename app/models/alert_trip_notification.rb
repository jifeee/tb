class AlertTripNotification < ActiveRecord::Base
	belongs_to :trip
	belongs_to :event
	
	before_update do |record|
		self.counter = self.counter.next
	end

end
