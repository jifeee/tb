module AppResources

  def possible_permissions
    @permissions = []
    Dir.new("#{Rails.root}/app/controllers").entries.each do |controller|
      if controller =~ /_controller/
        controller.gsub!('_controller.rb', '')
        next if skip?(controller) || !routes_hash.keys.include?(controller)
        
        routes_hash[controller].each do |action|
          @permissions << Permission.find_or_create_by_action_and_subject_class(action_alias(action), to_subj_class(controller))
        end
      end
    end
    @permissions.uniq
  end
  
  private
  def routes_hash
    @routes ||= Rails.application.routes.routes.map(&:defaults).inject({}) do |rez, route|
      (rez[route[:controller]] ||= []) << route[:action]; rez
    end
  end
  
  def to_subj_class name
    resource_alias(name).singularize.capitalize
  end
  
  def action_alias action
    {:batch_delete => :destroy,
     :new          => :create,
     :edit         => :update,
     :index        => :read,
     :show         => :read}[action.to_sym] || action
  end

  def resource_alias name
    {"parents" => "users"}[name] || name
  end
  
  def skip? controller
    ["application", "pages"].include? controller.downcase
  end
end
