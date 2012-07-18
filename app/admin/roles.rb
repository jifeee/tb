ActiveAdmin.register Role do
  actions :index, :show, :edit, :update

  # table for index action
  index do
    column :name
    column "Permissions" do |role|
      div :for => role do      
        (role.admin? ? Permission.possible_permissions : role.permissions).group_by{|p| permission_alias(p.subject_class)}.each do |group|
          b group.first.pluralize + ": "
          span group.last.map(&:action).join ', '
          br
        end
      end
    end
    column "Actions" do |role|
      link_to("View", admin_role_path(role)) << " " << link_to("Edit", edit_admin_role_path(role))
    end
  end

  # table for show action
  show :title => :name do
    attributes_table :id, :name

    panel "Permissions" do
      render '/roles/permissions'
    end

    active_admin_comments
  end
  
  # form for new/edit actions
  form :partial => '/roles/form'
end

