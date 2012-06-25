# Admin pages for textbusters
ActiveAdmin.register Device, :as => "Textbuster" do
  actions :index, :show, :edit, :update, :destroy
  
  # table for index action
  index do
    column :name
    column :imei
    column "Phones" do |d| 
      d.phones.map(&:name).join("<br/>").html_safe
    end
    default_actions
  end

  # table for show action
  show :title => :name do |device|
    attributes_table do
      row :name
      row "Phones" do 
        device.phones.map(&:name).join("<br/>").html_safe
      end
    end
    active_admin_comments
  end

  # form for new/edit actions
  form do |f|
    f.inputs "Textbuster Details" do
      f.input :name
      f.input :imei
      f.input :phones, :as => :check_boxes, :collection => f.object.family.try(:phones)
    end
    f.buttons do
      f.submit "Update Textbuster"
    end
  end

  # available filters
  filter :name
end

