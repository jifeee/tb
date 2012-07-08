namespace :alerts do
  task :time,  :event_datetime, :needs => :environment do |t, args|
  	puts "Hello"
  	puts args[:event_datetime]

  	alerts = Event.find(10790)
  	p alerts

  end
end