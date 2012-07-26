FactoryGirl.define do
  factory :event do |f|
    f.phones_log_id 1
    f.time {Time.now.to_i}
    f.locked 1
    f.textbuster_mac 'asd'
    f.locations_id 1
    f.type 0
  end
end