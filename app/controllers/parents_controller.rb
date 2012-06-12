class ParentsController < ApplicationController
  layout "parents"

  respond_to :html

  def new
    @parent = @family.parents.build
  end
  
  def create
    @parent = @family.parents.build(params[:user])
    respond_with(@parent) do |format|
      format.html do
        if @parent.save
          redirect_to @family
        else
          flash[:action] = :new
          flash[:errors] = @parent
          redirect_to family_path(current_user.family_id || 1)
        end
      end
    end
  end
end
