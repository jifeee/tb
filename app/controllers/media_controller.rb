class MediaController < ApplicationController
	
	SEND_FILE_METHOD = :apache
  
  def show
  	file_name = "#{params[:file_name]}.#{params[:type]}"
  	send_file_options = {}
    case SEND_FILE_METHOD
      when :apache then send_file_options[:x_sendfile] = true
      when :nginx then head(:x_accel_redirect => path.gsub(Rails.root, ''), :content_type => send_file_options[:type]) and return
    end
    send_file(File.join(Rails.root,'public/videos',file_name), send_file_options)
rescue
	redirect_to root_path


  end

end
