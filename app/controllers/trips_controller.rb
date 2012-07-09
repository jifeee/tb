# Trips controller. Showing the kids trips details to parents
class TripsController < ApplicationController
  load_and_authorize_resource
  layout "parents"
  authorize_resource
  
  # list of trips
  def index
    @trips = @family.trips
    if filter = params[:events_filer]
p "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"      
      @trips = @trips.where(:phone_id => filter[:phone]) if filter[:phone].present?
      @trips = @trips.where(:device_id => filter[:device]) if filter[:device].present?
    end
    @trips = @trips.page(params[:page]).per(20)
  end
  
  # trip detailes view
  def show
    @trip = Trip.find_by_id params[:id]
  end
end
