# Events controller
class EventsController < ApplicationController
  layout "parents"
  load_and_authorize_resource
  
  # events list. All events for user phones, can be filtered by phone and textbuster
  def index
    @events = Event.where(:phones_log_id => current_user.family.phones.event_phones) rescue nil
    if filter = params[:event]
      @events = @events.where(:phone_id => filter[:phone_id]) if filter[:phone_id].present?
      @events = @events.where(:device => filter[:device]) if filter[:device].present?
    end

  end
end
