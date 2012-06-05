# Events controller
class EventsController < ApplicationController
  layout "parents"
  load_and_authorize_resource
  
  # events list. All events for user phones, can be filtered by phone and textbuster
  def index
    @events = Event.where(:phone_id => current_user.family.phones)
    if filter = params[:event]
      @events = @events.where(:phone_id => filter[:phone_id]) if filter[:phone_id].present?
      @events = @events.where(:device_id => filter[:device_id]) if filter[:device_id].present?
    end
  end
end
