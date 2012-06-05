# API trips controller. Sending a trip / trips list as json
class Api::TripsController < ApplicationController
  # before_filter :authorize_user
  # authorize_resource

  # list of trips for the particular kid
  # http://localhost:3001/api/trips?token=e4e5391a1f89a3499b00f54cba87df3b&date_start=2012-01-01&date_end=2012-01-13&order=miles
  def index
    user = User.find_by_authentication_token(params[:token])
    trip = user.trips.scoped
    trip = trip.where(:created_at => (params[:date_start].to_date)..(params[:date_end].to_date)) unless params[:date_start].nil? && params[:date_end].nil? rescue trip
    trip = trip.order(params[:miles] == 'miles' ? 'distance' : 'created_at')
    render :json => trip.map(&:to_hash)
  rescue => e
    render :json => {:status => 403, :message => e.message}
  end
  
  # trip details
  def show
    user = User.find_by_authentication_token(params[:token])
    trip = user.trips.find_by_id(params[:id])
    render :json => {
      :trip_id => trip.id,
      :distance => trip.distance,
      :average_speed => trip.average_speed,
      :start => trip.start_point,
        # {:latitude => trip.start_point.latitude,
        # :longitude => trip.start_point.longitude,
        # :address => trip.start_point.address,
        # :city => '',
        # :zip => '',
        # :country => '',
        # :date => trip.start_point.created_at.try(:to_date),
        # :time => trip.start_point.created_at.try(:to_time)},
      :end => trip.end_point,
      # {:latitude => trip.end_point.latitude,
      #   :longitude => trip.end_point.longitude,
      #   :address => trip.end_point.address,
      #   :city => '',
      #   :zip => '',
      #   :country => '',
      #   :date => trip.end_point.created_at.try(:to_date),
      #   :time => trip.end_point.created_at.try(:to_time},
      :points => [{},{}]
    }
  rescue => e
    render :json => {:status => 403, :message => e.message}
  end
end
