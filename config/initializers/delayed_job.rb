Delayed::Worker.delay_jobs = !Rails.env.test?

Thread.new do
  Delayed::Worker.new.start
end

