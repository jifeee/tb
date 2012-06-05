# Trips controller. Showing the kids trips details to parents
class TripsController < ApplicationController
  layout "parents"
  authorize_resource
  
  # list of trips
  def index
    @trips = @family.trips
    if filter = params[:event]
      @trips = @trips.where(:phone_id => filter[:phone_id]) if filter[:phone_id].present?
      @trips = @trips.where(:device_id => filter[:device_id]) if filter[:device_id].present?
    end
  end
  
  # trip detailes view
  def show
    @trip = Trip.find_by_id params[:id]
  end
end
