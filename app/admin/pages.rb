# Admin pages for users
ActiveAdmin.register Page do
  before_filter do
    Page.class_eval do
      def to_param
        id.to_s
      end
    end
  end

  controller do
    uses_tiny_mce :options => { :theme => 'advanced', :theme_advanced_resizing => true, :theme_advanced_resize_horizontal => false, :plugins => %w{ table fullscreen style }}
  end

  # table for index action
  index do
    column :title
    column "Logged in", :menu_logged_in
    column "Logged out", :menu_logged_out
    column "Footer", :menu_footer
    column "Footer", :menu_main
    column "Data" do |page|
      truncate(page.data.gsub(/\<[^>]*\>/, ' '), :length => 100)
    end
    default_actions
  end

  # table for show action
  show :title => :title do
    attributes_table :title

    panel "Data" do
      render '/pages/show'
    end

    active_admin_comments
  end

  # form for new/edit actions
  form :partial => '/pages/form'

  # available filters
  filter :title
  filter :ancor
  filter :created_at
end

