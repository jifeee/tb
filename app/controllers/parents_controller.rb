class ParentsController < ApplicationController
  # authorize_resource
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

  def update
    user = @family.parents.find_by_id(params[:parent_id])
    respond_with(user) do |format|
      format.json do
        user.update_attributes params[:parent]
        render :json => {:status => 200, :user => user}
      end
    end
  end

end
