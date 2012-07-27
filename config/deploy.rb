# ssh textbuster.mobilezapp.de -l root
# c9J0mcCQ1

task :tst2 do
  set :application, "textbuster"
  set :domain, "176.9.79.105"
  server domain, :app, :web, :db, :primary => true

  set :deploy_to, "/var/www/#{application}"
  set :user, "root"
  set :password, "manan653"
  set :keep_releases, 2

  # set :rails_env, "test"
  set :rails_env, "production"
  set :scm, "git"
  set :repository, "https://juliavetl:VbifRheu12@github.com/mobilezapp/Access2.git"
  set :branch, "webapp_master"
  set :deploy_via, :checkout
  set :git_shallow_clone, 1
  default_run_options[:pty] = true

  set :unicorn_pid, "#{deploy_to}/tmp/pids/unicorn.pid"
  set :unicorn_cfg, "#{current_path}/config/unicorn.rb"

  namespace :unicorn do
    desc "start unicorn"
    task :start, :roles => :app, :except => {:no_release => true} do
      run "cd #{current_path} && bundle exec unicorn_rails -o 127.0.0.1 -c #{unicorn_cfg} -E #{rails_env} -D -p 3000"
    end
    desc "stop unicorn"
    task :stop, :roles => :app, :except => {:no_release => true} do
      run "kill -9 `cat #{unicorn_pid}`"
    end

    desc "unicorn restart"
    task :restart, :roles => :app, :except => {:no_release => true} do
      run "kill -9 `cat #{unicorn_pid}`"
      sleep 6
      run "cd #{current_path} && bundle exec unicorn_rails -o 127.0.0.1 -c #{unicorn_cfg} -E #{rails_env} -D -p 3000"
    end

    desc "unicorn reload"
    task :reload, :roles => :app, :except => {:no_release => true} do
      run "#{current_path}/bundle exec unicorn_exec reload"
    end
    desc "graceful stop unicorn"
    task :graceful_stop, :roles => :app, :except => {:no_release => true} do
      run "#{try_sudo} kill -s QUIT `cat #{unicorn_pid}`"
    end
  end

  namespace :deploy do
    task :start, :roles => :app do
      run "touch #{File.join(current_path,'tmp','restart.txt')}"
    end
    task :stop, :roles => :app do
      # do something
    end
    desc "Restart Application"
    task :restart, :roles => :app, :except => { :no_release => true } do
      run "touch #{File.join(current_path,'tmp','restart.txt')}"
    end
  end

  after "deploy:update_code", 'bundler:bundle_install',
                              "deploy:migrate",
                              :cleanup

  after 'deploy:restart', 'unicorn:restart'
  
  namespace :bundler do
    task :bundle_install, :roles => :app do
      run "cd #{current_path} && bundle install"
    end

    task :calculate_trips do
      run "cd #{current_path} && RAILS_ENV=#{rails_env} bundle exec rake trips:calculate"
    end
  end

  desc "Config symlinks"
  task :symlink_config_files do
    run "ln -nfs #{deploy_to}/#{shared_dir}/config/database.yml #{current_path}/config/database.yml"
  end

  desc "Fix dirs permission"
  task :fix_public_dir_permission do
    run "chmod -R g+w #{current_path}/public"
  end    
  task :fix_tmp_dir_permission do
    run "chmod -R a+rw #{current_path}/tmp"
  end

  desc <<-DESC
  Removes unused releases from the releases directory. By default, the last 5
  releases are retained, but this can be configured with the 'keep_releases'
  variable. This will use sudo to do the delete by default, but you can specify
  that run should be used by setting the :use_sudo variable to false.
  DESC
  task :cleanup do
    count = (self[:keep_releases] || 5).to_i
    if count >= releases.length
      logger.important "no old releases to clean up"
    else
      logger.info "keeping #{count} of #{releases.length} deployed releases"
      directories = (releases - releases.last(count)).map { |release| File.join(releases_path, release) }.join(" ")
      run "rm -rf #{directories}"
    end
  end

end






task :tst do
  set :application, "textbuster.mobilezapp.com"
  set :domain, "textbuster.mobilezapp.de"
  server domain, :app, :web, :db, :primary => true

  set :deploy_to, "/mnt/www/#{application}"
  set :user, "root"
  set :password, "c9J0mcCQ1"
  set :keep_releases, 2

  # set :rails_env, "test"
  set :rails_env, "production"
  set :scm, "git"
  set :repository, "https://juliavetl:VbifRheu12@github.com/mobilezapp/Access2.git"
  set :branch, "webapp_master"
  set :deploy_via, :checkout
  set :git_shallow_clone, 1
  default_run_options[:pty] = true

  set :unicorn_pid, "#{deploy_to}/tmp/pids/unicorn.pid"
  set :unicorn_cfg, "#{current_path}/config/unicorn.rb"

  namespace :unicorn do
    desc "start unicorn"
    task :start, :roles => :app, :except => {:no_release => true} do
      run "cd #{current_path} && bundle exec unicorn_rails -o 127.0.0.1 -c #{unicorn_cfg} -E #{rails_env} -D -p 3000"
    end
    desc "stop unicorn"
    task :stop, :roles => :app, :except => {:no_release => true} do
      run "kill -9 `cat #{unicorn_pid}`"
    end

    desc "unicorn restart"
    task :restart, :roles => :app, :except => {:no_release => true} do
      run "kill -9 `cat #{unicorn_pid}`"
      sleep 6
      run "cd #{current_path} && bundle exec unicorn_rails -o 127.0.0.1 -c #{unicorn_cfg} -E #{rails_env} -D -p 3000"
    end

    desc "unicorn reload"
    task :reload, :roles => :app, :except => {:no_release => true} do
      run "#{current_path}/bundle exec unicorn_exec reload"
    end
    desc "graceful stop unicorn"
    task :graceful_stop, :roles => :app, :except => {:no_release => true} do
      run "#{try_sudo} kill -s QUIT `cat #{unicorn_pid}`"
    end
  end

  namespace :deploy do
    task :start, :roles => :app do
      run "touch #{File.join(current_path,'tmp','restart.txt')}"
    end
    task :stop, :roles => :app do
      # do something
    end
    desc "Restart Application"
    task :restart, :roles => :app, :except => { :no_release => true } do
      run "touch #{File.join(current_path,'tmp','restart.txt')}"
    end
  end

  after "deploy:update_code", :symlink_config_files,
                              'bundler:bundle_install',
                              :fix_public_dir_permission,
                              :fix_tmp_dir_permission,
                              "deploy:migrate",
                              :cleanup

  after 'deploy:restart', 'unicorn:restart'
  
  namespace :bundler do
    task :bundle_install, :roles => :app do
      run "cd #{current_path} && bundle install"
    end

    task :calculate_trips do
      run "cd #{current_path} && RAILS_ENV=#{rails_env} bundle exec rake trips:calculate"
    end
  end

  desc "Config symlinks"
  task :symlink_config_files do
    run "ln -nfs #{deploy_to}/#{shared_dir}/config/database.yml #{current_path}/config/database.yml"
  end

  desc "Fix dirs permission"
  task :fix_public_dir_permission do
    run "chmod -R g+w #{current_path}/public"
  end    
  task :fix_tmp_dir_permission do
    run "chmod -R a+rw #{current_path}/tmp"
  end

  desc <<-DESC
  Removes unused releases from the releases directory. By default, the last 5
  releases are retained, but this can be configured with the 'keep_releases'
  variable. This will use sudo to do the delete by default, but you can specify
  that run should be used by setting the :use_sudo variable to false.
  DESC
  task :cleanup do
    count = (self[:keep_releases] || 5).to_i
    if count >= releases.length
      logger.important "no old releases to clean up"
    else
      logger.info "keeping #{count} of #{releases.length} deployed releases"
      directories = (releases - releases.last(count)).map { |release| File.join(releases_path, release) }.join(" ")
      run "rm -rf #{directories}"
    end
  end

end