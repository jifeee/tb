FactoryGirl.define do
  factory :user do |f|
  	f.after_build {|u| u.class.skip_callback(:create, :before, :fill_defaults)}
  	f.after_build {|u| u.class.skip_callback(:create, :after, :send_welcome_email)}

    f.email 'user@mail.com'
    f.role_id 2
    f.family_id 1
    f.is_main true
	f.password 'password'
	f.name 'user@mail.com'
  end
end