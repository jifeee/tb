# Admin pages for textbusters
ActiveAdmin.register Device.unscoped, :as => 'Textbuster' do
  actions :index, :show, :edit, :update, :destroy

  collection_action :active_admin_collection do
    Device.unscoped{super}
  end

  # scoped tabs
  scope :all, :dafailt => true do |devices| devices.unscoped end
  scope 'Active' do |devices| devices.where(:is_deleted => false) end
  scope 'Marked as delete', :with_deleted

  # available filters
  filter :name
  
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
    end
    f.buttons do
      f.submit "Update Textbuster"
    end
  end
end

