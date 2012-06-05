# Common methods
class ApplicationController < ActionController::Base
  protect_from_forgery
  before_filter :restore_page_param, :retrieve_family
  
  # redirection for user attempting to get restricted resources
  rescue_from CanCan::AccessDenied do |exception|
    redirect_to root_url, :alert => exception.message
  end
  
  #root page (temporary)
  def main
  end
  
  protected
  # restrict access to admin module for non-admin users
  def authenticate_admin_user
    authorize! :manage, :all
  end
  
  # path for redirection after user sign_in, depending on user role
  def after_sign_in_path_for(user)
  	user.admin? ? admin_dashboard_path : family_path(user.family_id||1)
  end

  # retrieve user by authentication token (for API authorisation)  
  def authorize_user
    if params[:token] && (user = User.find_by_authentication_token(params[:token]))
      sign_in(:user, user)
    else
      render :json => {:status => 403, :message => 'Authentication token invalid'}
    end
  end
  
  protected
  # restore page slug changed by admin controller
  def restore_page_param
    Page.class_eval do
      def to_param
        anchor
      end
    end
  end
  
  # get @family if available
  def retrieve_family
    @family = Family.first if current_user.try(:admin?)
    if user_signed_in? && current_user.family_id
      @family = Family.find_by_id current_user.family_id
    end
  end
end
