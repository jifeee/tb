namespace :alerts do
  task :t1, :event_datetime, :needs => :environment do |t, args|
  	@force = "test"
  end

  task :t2 => :t1 do |t|
  	aaa('bbb')
  	puts "Hello, (#{@force})"
  end

  def aaa(force = nil)

  end

end
 