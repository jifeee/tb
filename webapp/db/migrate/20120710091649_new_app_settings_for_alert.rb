class NewAppSettingsForAlert < ActiveRecord::Migration
  def self.up
  	setting = Setting.create(:name => 'alert_notification_email_from', :value => 'alert@textbuster.com')
  	setting = Setting.create(:name => 'customerservice_email', :value => 'customerservice@textbuster.com')
  end

  def self.down
  	Setting.where(:name => ['alert_notification_email_from','customerservice_email']).delete_all
  end
end
