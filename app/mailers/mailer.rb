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
end
