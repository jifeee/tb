# Admin pages for trips resource
ActiveAdmin.register Trip do
  # table view for index action
  index do
    column :user, :sortable => false
    column :device, :sortable => false
    column :distance
    column :average_speed
    column :start_point, :sortable => false
    column :end_point, :sortable => false
    
    default_actions
  end
  
  # form for edit/new action
  form do |f|
    f.inputs "Trip Details" do
      f.input :user
      f.input :device
      f.input :distance
      f.input :average_speed
    end
    f.buttons
  end
  
  # table for show action
  show do
    attributes_table do
      row :user
      row :device
      row :distance
      row :average_speed
      row :start_point
      row :end_point
    end
    
    panel "Route Details" do
      render '/trips/route'
    end
    
    active_admin_comments
  end
  
  # available filters
  filter :user
  filter :device
  filter :distance
  filter :average_speed
  filter :created_at
end
