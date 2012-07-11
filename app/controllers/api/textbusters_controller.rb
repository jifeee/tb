class Api::TextbustersController < ApplicationController

	def create
		user = User.find_by_authentication_token(params[:token])
		if user
			phone = user.family.phones.where(:imei => params[:imei])
			device = user.family.devices.new :imei => params[:mac], 
				:name => params[:mac]
			if device.valid? && device.save
				device.phones << phone
				render :json => {:status => 200}
			else
				render :json => {:status => 400, :message => device.errors.full_messages.first}
			end
		else
			render :json => {:status => 400, :message => 'User was not found'}			
		end
	rescue => e
		render :json => {:status => 401, :message => e.message}
	end

end