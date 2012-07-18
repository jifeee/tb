# Admin pages for phones
ActiveAdmin.register Phone do
  actions :index, :show, :edit, :update, :destroy
  
  # table for index action
  index do
    column :name
    column :user
    column(:abilities)  {|p| p.abilities.join(', ')}
    column "Textbusters" do |p| 
      p.devices.map(&:display_name).join("<br/>").html_safe
    end
    column "Alerts" do |p|
      p.alerts.map(&:name).join("<br/>").html_safe
    end
    column :created_at
    default_actions
  end

  # table for show action
  show :title => :name do |phone|
    attributes_table do
      row :name
      row :user
      row(:abilities)  { phone.abilities.join(', ')}
      row "Textbusters" do 
        phone.devices.map(&:display_name).join("<br/>").html_safe
      end
      row "Alerts" do
        phone.alerts.map(&:name).join("<br/>").html_safe
      end
      row :created_at
    end
    active_admin_comments
  end

  # form for new/edit actions
  form :partial => '/phones/form'

  # available filters
  filter :name
  filter :user
  filter :created_at
end

