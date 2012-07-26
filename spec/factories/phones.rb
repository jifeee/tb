FactoryGirl.define do
  factory :phone do |f|
    f.imei 123456789
    f.name 'some phone name'
  end
end