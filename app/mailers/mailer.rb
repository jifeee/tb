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

  def alert_notification(time,alert,phones_log,event,trip)
    @contact = Setting.find_by_name('customerservice_email').value
    @time = time
    from = Setting.find_by_name('alert_notification_email_from').value
    emails = alert.users.map {|user| user.email}
    subject = "You are being sent an alert message from TextBuster ® for the phone user #{phones_log.phone.name}"
    mail(:to => emails, :from => from, :subject => subject)

    #  Storing aler history
    alert_history = AlertHistory.create :trip_id => trip.id, :event_id => event.id
    alert.alert_histories << alert_history    
  end

end
