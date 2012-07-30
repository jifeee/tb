# Admin pages for users
ActiveAdmin.register User do

  controller do
    def update
      if params[:user][:password].blank?
        params[:user].delete(:password)
        params[:user].delete(:password_confirmation) 
      end
      super
    end    
  end

  # table for index action
  index do
    column 'Main' do |user|
      image_tag 'icons/boss.png' if user.is_main?
    end
    column :email
    column :login
    column :name
    column :phone
    column :role, :sortable => :role_id
    column :current_sign_in_at
    column :last_sign_in_at
    column :sign_in_count
    column 'SMS', :is_sms
    column 'Allowed SMS', :is_allowed_sms
    default_actions
  end

  # table for show action
  show :title => :email do
    attributes_table do
      row :name
      row :login
      row :phone
      row :role
      row :is_sms
      row :is_allowed_sms
    end
    active_admin_comments
  end

  # form for new/edit actions
  form do |f|
    f.inputs "User Details" do
      f.input :email
      f.input :name
      f.input :password
      f.input :password_confirmation
      f.input :login
      f.input :phone
      f.input :is_sms
      f.input :is_allowed_sms
    end
    f.buttons
  end

  # show kids in separate panel for the show action, depending on user role
  sidebar "Relations Details", :only => :show do
    unless user.admin?
      b user.parent? ? "Kids watched by this parent" : "Parents watching this kid"
      hr

      collection = user.parent? ? user.kids : user.parents

      table_for collection do |t|
        t.column(:email)
        t.column(:name) { |child| link_to(child.name, admin_user_path(child)) }
      end if collection
    end
  end

  # show parents/kids in separate panel for the show action, depending on user role
  sidebar "Family", :only => :show do
    unless user.admin?
      collection = user.parents

      table_for collection do |parent|
        parent.column(:name) {|p| link_to(p.name, admin_user_path(p))}
        parent.column do |p|
          image_tag 'icons/boss.png' if p.is_main?
        end
      end if collection
    end
  end

  # available filters
  filter :name
  filter :login
  filter :email
  filter :phone
  filter :created_at
  filter :last_sign_in_at

  # tabs to group users by roles
  scope :all, :default => true
  scope :admins do |users| users.where(:role_id => Role.by_name(:admin)) end
  scope :account_managers do |users| users.where(:role_id => Role.by_name(:parent), :is_main => true) end
end

