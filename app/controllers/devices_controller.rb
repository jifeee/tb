# Devices (textbusters) controller
class DevicesController < ApplicationController
  layout "parents"
  load_and_authorize_resource

  respond_to :html, :js
  
  # events list. All events for user phones, can be filtered by phone and textbuster
  def index
    @devices = @family.devices
  end
  
  def new
    @device = Device.new
    @device.phone_ids = params[:phone_ids] if params[:phone_ids]
    respond_with(@device) do |format|
      format.js
    end
  end
  
  def create
    @device = Device.new params[:device]
    @device.family_id = current_user.family_id
    respond_with(@device) do |format|
      format.js do
        if @device.save
          render :update do |page|
            page << "window.location.href = '#{textbusters_path}';"
          end
        else
          render :new
        end
      end
    end
    
  end
  
  def update
    @device.phones.clear
    if @device.update_attributes params[:device]
      redirect_to textbusters_path
    else
      render :action => :edit
    end
  end
  
  def batch_delete
    Device.destroy_all(:id => params[:ids].split(','))
    redirect_to textbusters_path
  end
end
