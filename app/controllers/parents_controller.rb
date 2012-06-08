class ParentsController < ApplicationController
  layout "parents"
  def new
    @parent = @family.parents.build
  end
  
  def create
    @parent = @family.parents.build(params[:user])
    if @parent.save
      redirect_to @family
    else
      render :action => :new
    end
  end
end
