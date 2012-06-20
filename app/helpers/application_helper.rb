module ApplicationHelper

  def brand(applogo)
    content_tag "div", :class => "brand" do 
      root = current_user.try(:admin?) ? admin_dashboard_path : root_path
      concat link_to image_tag("logo.png", :alt => "TextBuster", :width => 181, :height => 21), root, :class => 'logo'
      concat link_to image_tag("applogo.s.png", :width => 28, :height => 28), root_path, :class => 'applogo' if applogo
    end
  end

  def menu_item_label meth, model
    "<span class='label label-important'>#{@family.send(meth).count}</span> #{MenuItem.find_by_resource(model).menu_item.capitalize}".html_safe
  end
  
  def menu_item_class ctr
    "active" if ctr.split('/').include? controller_name
  end
  
  def permission_alias resource
    {"User" => "Parent",
     "Family" => "Parent"}[resource] || resource
  end

  def errors_for(object, message=nil)
    html = ""
    unless object.errors.blank?
      html << "<div class='alert alert-error'>\n"
      html << "<ul>\n"
      object.errors.full_messages.each do |error|
        html << "<li>#{error}</li>\n"
      end
      html << "</ul>\n"
      html << "</div>\n"
    end
    html.html_safe
  end
end
