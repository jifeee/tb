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
    parents = alert.users
    emails = parents.map {|user| user.email}
    puts "send alerts for #{emails.join(',')}"
    subject = "You are being sent an alert message from TextBuster ® for the phone user #{phones_log.phone.name}"
    mail(:to => emails, :from => from, :subject => subject)

    #  Storing aler history
    alert_history = AlertHistory.create :trip_id => trip.id, :event_id => event.id
    alert.alert_histories << alert_history    

    #  Sending SMS
    parents.map do |parent|
      puts "send sms to  #{parent.phone}" if parent.phone
      send_sms(parent.phone, subject) if parent.phone
    end
  end

  def system_notification(id,family_id,device,phone)
    user = User.where(:family_id => family_id).main.first
    if user 
      email = user.email
      subject = id.humanize.capitalize
      mail :to => email, :subject => subject, :body => "#{subject}, device: #{device.id rescue 'undefined'}, phone: #{phone.id rescue 'undefined'}"
    else
      return false
    end
  end

  def send_sms(number,message)
    puts "... sending sms to #{number}"
    sms_route_url = 'http://smsc5.routotelecom.com/SMSsend'
    user = Setting.find_by_name('sms_username').value
    pass = Setting.find_by_name('sms_password').value
    response = RestClient.post sms_route_url, 
      {
        :number => number, 
        :user => user,
        :pass => pass,
        :message => message
      }     
    puts "#{user},#{pass}, response: #{response.body}"
  end

end
