class Mailer < ActionMailer::Base
  include Devise::Mailers::Helpers

  default :from => "info@textbuster.com"
  
  def registered(user)
    @user = user
    mail(:to => user.email, :subject => "Registered")  
  end

  def reset_password_instructions(record)
    devise_mail(record, :reset_password_instructions)
  end

  def alert_time_restriction(alert,device,phones_log,trip)
    puts 'alert_time_restriction.....................................................'
    @user = phones_log.phone.user
    @device = device
    @trip = trip
    alert.users.map do |user|
    	subject = 'Alert time restriction. Don\'t answer this message'
      puts "alert ,ailinig to #{user.email}"
    	mail(:to => user.email, :subject => subject)
    end
  end

end
