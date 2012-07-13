# Trips controller. Showing the kids trips details to parents
class TripsController < ApplicationController
  load_and_authorize_resource
  layout "parents"
  authorize_resource
  
  # list of trips
  def index
    @trips = @family.trips.where(:phone_id => current_user.family.phones, :device_id => current_user.family.devices)
    # @trips = Event.where(:phones_log_id => current_user.family.phones.event_phones) rescue nil
    if filter = params[:events_filer]
      @trips = @trips.where(:phone_id => filter[:phone]) if filter[:phone].present?
      @trips = @trips.where(:device_id => filter[:device]) if filter[:device].present?
    end
    @trips = @trips.order('updated_at DESC').page(params[:page]).per(20)
  end
  
  # trip detailes view
  def show
    @trip = Trip.find_by_id params[:id]
  end
end
