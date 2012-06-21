# Admin pages for global application settings
ActiveAdmin.register Setting do
  actions :index, :show, :edit
  # table view for index action
  index do
    column :name
    column :value
    
    column "Actions" do |set|
      link_to("View", admin_setting_path(set)) << ' ' << link_to("Edit", edit_admin_setting_path(set))
    end
  end
  
  # form for edit/new actions
  form do |f|
    f.inputs "Global application param" do
      f.input :name, :input_html => {:disabled => true, :style => "color:#000;"}
      f.input :value
    end
    f.buttons
  end
  
  # table for show action
  show do
    attributes_table do
      row :name
      row :value
    end
    
    active_admin_comments
  end
end
