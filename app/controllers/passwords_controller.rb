class PasswordsController < ApplicationController
	respond_to :html, :js

	def reset
		user = User.find_by_email(params[:email])
		Mailer.reset_password_instructions(user).deliver
		respond_with(user) do |format|
			format.js {render :nothing => true}
			format.any {render :nothing => true}
		end
	end

  def edit
    @user = current_user
    respond_with(@user) do |format|
    	format.js {}
    end
  end

  def update
    @user = current_user
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
	end
  end

end
