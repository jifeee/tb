# Static pages controller
class PagesController < ApplicationController 
  
  # show static page
  def show
    @page = Page.find_by_anchor params[:id]
    redirect_to root_page unless @page
  end
end
