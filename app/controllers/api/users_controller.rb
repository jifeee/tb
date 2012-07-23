# A controller to handle user sessions
class Api::UsersController < ApplicationController

  before_filter :logger

  # login action
  # http://localhost:3000/api/login?email=isbaysoft@gmail.com&password=qwe123
  #  ID = 2
  def login
    user = User.find_by_email(params[:email])
    if user && user.valid_password?(params[:password])
      user.reset_authentication_token!
      user_sign_in user, params[:imei]
      
      render_with_log :json => {:token => user.authentication_token, :devices => user.devices.flatten.uniq.map(&:imei)}
    else
      render_with_log :json => {:status => 403, :message => 'email and/or password is incorrect'}
    end
  end

  # http://localhost:3001/api/sign_up?email=isbaysoft@gmail.com&password=qwe123
  def sign_up
    phone = Phone.find_by_imei params[:device]
    unless phone.nil?
      render_with_log :json => {:status => 403, :message => 'Imei has already been taken'} and return
    end
    user = User.new :email => params[:email], 
      :password => params[:password],
      :name => params[:email],
      :login => params[:email]
    if user && user.valid_password?(params[:password]) && user.save
      user.phones << Phone.create(:imei => params[:device])
      user.reset_authentication_token!
      render_with_log :json => {:token => user.authentication_token, :user_id => user.id}
    else
      render_with_log :json => {:status => 403, :message => user.errors.full_messages.join(', ')}
    end
  end

  def change_password
    user = User.find_by_authentication_token(params[:token])
    raise 'User not found or incorrect token' unless user
    raise 'Password is incorrect' unless user.valid_password?(params[:current_password])
    if user.reset_password!(params[:new_password],params[:confirm_new_password])
      render_with_log :json => {:token => user.authentication_token}
    else
      render_with_log :json => {:status => 403, :message => user.errors.full_messages.join(', ')}
    end    
  rescue => e
    render_with_log :json => {:status => 403, :message => e.message}
  end

  def get_textbusters
    phone = Phone.find_by_imei(params[:imei])
    if phone 
      render_with_log :json => {:status => 200, :textbusters => phone.devices.select('imei').all.map(&:imei)}
    else
      render_with_log :json => {:status => 401, :message => 'Phone was not found for specified imei'}
    end
  rescue => e
    render_with_log :json => {:status => 403, :message => e.message}
  end

  def system_notification
    id = params[:id]

    unless %w(textbuster_uninstall guard_uninstall).include?(id)
      render :json => {:status => 401, :message => 'Undefined system notification type'} and return
    end

    family_id = nil
    if imei = params[:imei]
      phone = Phone.find_by_imei(imei)
      family_id = phone.user.family_id
    elsif mac = params[:mac]
      device = Device.find_by_imei(mac)
      family_id = device.family_id
    elsif token = params[:token]
      user = User.find_by_authentication_token(token)
      family_id = user.family_id      
    end

    unless family_id
      render :json => {:status => 401, :message => 'Family was not found'} and return 
    end

    if Mailer.system_notification(id,family_id,device,phone).deliver
      render :json => {:status => 200}
    else
      render :json => {:status => 401}
    end
  rescue => e
      render :json => {:status => 401, :message => e.message}    
  end

protected

  def render_with_log options = {}
    @logger.info "LOGIN_#{DateTime.now.strftime('%Y%m%d %H:%M:%S')}: response #{options.inspect}"
    render options
  end

  def user_sign_in user, imei
    sign_in(:user, user)
    Phone.find_or_create_by_imei_and_user_id(imei,user.id) if imei
  end

  def logger
    @logger.info "LOGIN_#{DateTime.now.strftime('%Y%m%d %H:%M:%S')}: params #{params.inspect}"    
  end

end
