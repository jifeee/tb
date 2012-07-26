FactoryGirl.define do
  factory :location do |f|
    f.after_build {|location| location.class.skip_callback(:save, :after, :update_address)}
    f.lat 49.628162
    f.lng 49.628162
    f.time 0
  end
end