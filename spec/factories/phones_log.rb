FactoryGirl.define do
  factory :phones_log do |f|
    f.id 1
    f.imei 123456789
    f.created {Time.now}
    f.seen {Time.now}
  end
end