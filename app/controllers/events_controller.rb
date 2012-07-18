# Events controller
class EventsController < ApplicationController
  layout "parents"
  load_and_authorize_resource
  
  # events list. All events for user phones, can be filtered by phone and textbuster
  def index
    @events = Event.where(:phones_log_id => current_user.family.phones.event_phones, 
      :textbuster_mac => current_user.family.devices.all.map {|d| d.imei}) rescue nil
    if filter = params[:events_filer]
      if filter[:phone].present?
        phones_log = Phone.find(filter[:phone]).phones_log.first
        @events = @events.where(:phones_log_id => phones_log.try(:id).to_i) 
      end
      if filter[:device].present?
        device = Device.find(filter[:device])
        @events = @events.where(:textbuster_mac => device.try(:imei).to_s) 
      end
    end
    @events = @events.order('time DESC').page(params[:page]).per(20)
  end
end