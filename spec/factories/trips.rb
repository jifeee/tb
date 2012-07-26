FactoryGirl.define do
  factory :trip do |f|
    f.distance 100
    f.average_speed 200
    f.start_point_id 1
    f.end_point_id 10
  end
end