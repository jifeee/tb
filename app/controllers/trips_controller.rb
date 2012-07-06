# Trips controller. Showing the kids trips details to parents
class TripsController < ApplicationController
  load_and_authorize_resource
  layout "parents"
  authorize_resource
  
  # list of trips
  def index
    @trips = @family.trips
    if filter = params[:event]
      @trips = @trips.where(:phone_id => filter[:phone_id]) if filter[:phone_id].present?
      @trips = @trips.where(:device => filter[:device]) if filter[:device].present?
    end
    @trips = @trips.page(params[:page])
  end
  
  # trip detailes view
  def show
    @trip = Trip.find_by_id params[:id]
  end
end
