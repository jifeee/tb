# coding: utf-8
# API trips controller. Sending a trip / trips list as json
class Api::TripsController < ApplicationController
  before_filter :logger

  # before_filter :authorize_user
  # authorize_resource

  # list of trips for the particular kid
  # http://localhost:3001/api/trips?token=e4e5391a1f89a3499b00f54cba87df3b&date_start=2012-01-01&date_end=2012-01-13&order=miles
  def index
    user = User.find_by_authentication_token(params[:token])
    trip = user.trips_without_order.scoped
    trip = trip.where(:created_at => (params[:date_start].to_date)..(params[:date_end].to_date)) unless params[:date_start].nil? && params[:date_end].nil? rescue trip
    trip = trip.order(params[:order] == 'miles' ? 'distance ASC' : 'created_at ASC')
    render_with_log :json => trip.map(&:to_hash)
  rescue => e
    render_with_log :json => {:status => 403, :message => e.message}
  end
  
  # trip details
  def show
    user = User.find_by_authentication_token(params[:token])
    trip = user.trips.find(params[:id])
    points = Location.where(:id => trip.start_point_id..trip.end_point_id).all.map {|l| {:longitude => l.lng, :latitude => l.lat}.to_json}
    render_with_log :json => {
      :trip_id => trip.id,
      :distance => trip.distance,
      :average_speed => trip.average_speed,
      :start => trip.start_point.api_point_detail,
      :end => trip.end_point.api_point_detail,
      :points => points
    }
  rescue => e
    case e
    when ::ActiveRecord::RecordNotFound
      render_with_log :json => {:status => 403, :message => 'Couldn\'t find Trip with ID=2'}
    else
      render_with_log :json => {:status => 403, :message => e.message}
    end
  end

protected

  def render_with_log options = {}
    @logger.info "LOGIN_#{DateTime.now.strftime('%Y%m%d %H:%M:%S')}: response #{options.inspect}"
    render options
  end

  def logger
    @logger.info "LOGIN_#{DateTime.now.strftime('%Y%m%d %H:%M:%S')}: params #{params.inspect}"    
  end

end
