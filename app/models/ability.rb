# class defining all user permissions
class Ability
  include CanCan::Ability

  def initialize(user)
    user ||= User.new
    alias_action :destroy, :to => :batch_delete
    
    if user.admin?
      can :manage, :all
      %w(Alert Device Event Family ParentsController Phone Restriction Trip).map do |obj|
        cannot :manage, obj.constantize
      end

    elsif user.role
      user.role.permissions.each do |permission|
        can permission.action.to_sym, permission.subject_class.constantize
      end
      #cannot manage other family
    end
    can :read, Page
    
  end
end
    
    
#      can :read, Family, :id => user.family_id
#      can :manage, Family, :id => user.family_id if user.parent?
#      
#      can :manage, :restrictions do |restr|
#        user.watches? restr.user
#      end
#      
#      can :read, Trip do |trip|
#        trip.user_id == user.id || user.watches?(trip.user)
#      end
#      can :read, Phone
#      can :manage, Phone do |phone|
#        user.family_id == phone.user.family_id
#      end
#      can :read, Event do |event|
#        user.family.members.map(&:phones).map(&:id).include? event.phone_id
#      end
#      can :manage, Device, :family_id => user.family_id


