# Restrictions controller: pages for children geolocation restrictions management, user should be authenticated.
class RestrictionsController < ApplicationController
  layout "parents"
  before_filter :find_owner
  
  # show all restrictions on the map, and
  # let the parent change them
  def index
    authorize! :read, @user.trips.build
    @restrictions = @user.restrictions
  end
  
  # change restrictions list for the kid
  def create
    @user.update_attributes params[:user]
    redirect_to user_restrictions_path(@user), :notice => "Restrictions saved"
  end
  
  protected
  # find user to display restrictions for
  def find_owner
    @user = User.find_by_id params[:user_id]
  end
end
