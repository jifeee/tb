# Phones mamnagement controller.
class PhonesController < ApplicationController
  layout "parents"
  load_and_authorize_resource
  
  # list of all the phones in the family
  def index
    @phones = @family.phones
  end
  
  def update
    @phone.devices.clear
    @phone.alerts.clear
    if @phone.update_attributes params[:phone]
      respond_to do |format|
        format.html { redirect_to phones_path }
        format.js { render :nothing => true }
      end
    else
      render :action => :edit
    end
  end
  
  def batch_delete
    Phone.destroy_all(:id => params[:ids].split(','))
    redirect_to phones_path
  end
end
