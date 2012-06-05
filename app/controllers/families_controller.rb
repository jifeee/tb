# Family account controller. These are pages for parents/children management, so user should be authenticated.
class FamiliesController < ApplicationController
  layout "parents"
  authorize_resource
  
  # update family data (including adding new members and changing their data)
  def update
    if @family.update_attributes(params[:family])
      redirect_to family_path(@family)
    else
      render :action => :edit
    end
  end
end
