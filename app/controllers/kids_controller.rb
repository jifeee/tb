class KidsController < ApplicationController
	layout "parents"

	respond_to :html, :js

	def index
		@family = current_user.family
		@kids = current_user.family.kids
	end

	def edit
		@family = current_user.family
		# @kids = current_user.family.kids.find(params[:id])
		@kid = User.find(params[:id])
		respond_with(@parent) do |format|
			format.js {render}
		end
	end

	def update
		if params[:user][:password].blank?
      params[:user].delete(:password)
      params[:user].delete(:password_confirmation) 
    end
		@family = current_user.family
		@kid = User.find(params[:id])
		respond_with(@parent) do |format|
			format.js do
		    if @kid.update_attributes params[:user]
		      render :update do |page| 
		        page << "window.location.href = '#{family_kids_path(@family || 1)}';"
		      end
		    else
		      render :edit
		    end
		  end
		end		
	end

end
