FactoryGirl.define do
  factory :alert do |f|
    f.name 'alert_time_restriction' 
    f.event_type 'Driving at a specific time'
    f.enabled 1
    f.restricted_time_start '1:00 AM'
    f.restricted_time_end '3:00 AM'
  end
end