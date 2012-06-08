# Admin pages for users
ActiveAdmin.register MenuItem do
  actions :index, :edit, :update

  # table for index action
  index do
    column :resource
    column :menu_item
    column "Last edited", :updated_at
    
    column "Actions" do |i|
      link_to 'Edit', edit_admin_menu_item_path(i)
    end
  end

  # table for show action
  show :title => :resource do
    attributes_table :resource

    active_admin_comments
  end

  # form for new/edit actions
  form do |f|
    f.inputs "Menu Item for #{f.object.resource}" do
      f.input :resource, :input_html => {:disabled => true, :style => "color:#000;"}
      f.input :menu_item
    end
    f.buttons
  end

  # available filters
  filter :resource
  filter :menu_item
  filter :created_at
end

