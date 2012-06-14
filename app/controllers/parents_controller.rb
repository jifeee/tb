class ParentsController < ApplicationController
  layout "parents"

  respond_to :html, :js

  def new
    @parent = @family.parents.new
    respond_with(@parent) do |format|
      format.js {render :new}
    end
  end
  
  def create
    @parent = @family.parents.build(params[:user])
    respond_with(@parent) do |format|
      format.js do
        if @parent.save
          render :update do |page| 
            page << "window.location.href = '#{family_path(current_user.family_id || 1)}';"
          end
        else
          render :new
        end
      end
    end
  end
end
