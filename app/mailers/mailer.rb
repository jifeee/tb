class Mailer < ActionMailer::Base
  default :from => "info@textbuster.com"
  
  def registered(user)
    @user = user
    mail(:to => user.email, :subject => "Registered")  
  end
end
