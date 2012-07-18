# Trips controller. Showing the kids trips details to parents
class TripsController < ApplicationController
  load_resource :except => [:destroy]
  layout "parents"
  authorize_resource :except => [:destroy]
  
  # list of trips
  def index
    # @trips = @family.trips.where(:phone_id => current_user.family.phones, :device_id => current_user.family.devices)
    @trips = @family.trips.includes([:start_point, :end_point, :phone, :alert_histories, :device])
    @trips = @trips.where(:phone_id => current_user.family.phones, :device_id => current_user.family.devices)

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

  def destroy
    trips = @family.trips.where(:phone_id => current_user.family.phones, :device_id => current_user.family.devices)
    trips = trips.where(:id => params[:ids].split(','))
    trips.destroy_all
    redirect_to trips_path    
  end

end
