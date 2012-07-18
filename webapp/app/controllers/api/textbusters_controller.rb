class Api::TextbustersController < ApplicationController

	def create
		user = User.find_by_authentication_token(params[:token])
		phone = user.family.phones.find_by_imei params[:imei]
		if user && phone
			device = user.family.devices.find_or_create_by_imei params[:mac]
			unless device.phones.include?(phone)
				device.phones << phone
				render :json => {:status => 200} and return
			else
				render :json => {:status => 200, :message => "already connected"} and return
			end
		else
			render :json => {:status => 400, :message => 'User was not found'}			
		end
	rescue => e
		render :json => {:status => 401, :message => e.message}
	end

end