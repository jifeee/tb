class PasswordsController < ApplicationController
	respond_to :html, :js

	def reset
		user = User.find_by_email(params[:email])
		user.send_reset_password_instructions
		respond_with(user) do |format|
			format.js {render :nothing => true}
			format.any {render :nothing => true}
		end
	end

  def edit
    @user = current_user || User.find(params[:id])
    respond_with(@user) do |format|
    	format.js {}
    	format.html {}
    end
  end

  def update
    if params[:user][:reset_password_token] && !params[:user][:reset_password_token].blank?
    	@user = User.find_by_id_and_reset_password_token(params[:id], params[:user][:reset_password_token])
    else
    	@user = current_user
	end
    respond_with(@user) do |format|
    	format.js do 
		    if @user.update_with_password(params[:user])
				sign_in(@user, :bypass => true)
				render :update do |page|
					page << "window.location.href = '#{family_path(current_user.family_id || 1)}';"
				end
		    else
		      render :edit
		    end
		end
		format.html do
			if @user.reset_password!(params[:user][:password],params[:user][:password_confirmation])
				sign_in(@user, :bypass => true) 
				redirect_to new_user_session_path
			else
				render :edit
			end
		end
	end
  end

end
