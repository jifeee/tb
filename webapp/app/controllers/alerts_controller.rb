# Alerts controller: pages for alerts settings
class AlertsController < ApplicationController
  layout "parents"
  before_filter :find_owner, :only => :index
  load_and_authorize_resource

  def new
    @alert = Alert.new(:phone_ids => [params[:phone_id]])
  end
  
  def create
    @alert = Alert.new params[:alert]
    @alert.author = current_user
    if @alert.save
      redirect_to alerts_path
    else
      render :action => :new
    end
  end
  
  def edit
    @alert = Alert.find_by_id params[:id]
  end
  
  def update
    @alert = Alert.find_by_id params[:id]
    @alert.phones.clear
    @alert.users.clear
    if @alert.update_attributes params[:alert]
      redirect_to alerts_path
    else
      render :action => :edit
    end
  end
  
  def show
    @alert = Alert.find_by_id params[:id]
  end
  
  # show all the alerts
  def index
    @alerts = @phone ? @phone.alerts : @family.authored_alerts
  end
  
  def batch_delete
    Alert.destroy_all(:id => params[:ids].split(','))
    redirect_to alerts_path
  end

  protected
  # find phone to display restrictions for
  def find_owner
    @phone = Phone.find_by_id params[:phone_id]
    true
  end  
end
