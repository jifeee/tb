# Admin pages for users
ActiveAdmin.register User do

  # table for index action
  index do
    column :email
    column :login
    column :name
    column :phone
    column :role, :sortable => :role_id
    column :current_sign_in_at
    column :last_sign_in_at
    column :sign_in_count
    default_actions
  end

  # table for show action
  show :title => :email do
    attributes_table do
      row :name
      row :login
      row :phone
      row :role
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
    end
    f.buttons
  end

  # show parents/kids in separate panel for the show action, depending on user role
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
  scope :parents do |users| users.where(:role_id => Role.by_name(:parent)) end
end

