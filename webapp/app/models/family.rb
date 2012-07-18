# Family account, represents all users connected to each other
class Family < ActiveRecord::Base
  has_many :devices
  has_many :phones, :through => :members
  has_many :trips, :through => :members
  has_many :users
  has_many :members, :class_name => "User", :order => :role_id
  has_many :authored_alerts, :through => :members
  accepts_nested_attributes_for :members, :allow_destroy => true, :reject_if => :all_blank
  
  # retrives the users in the family with 'Parent' role
  def parents
    members.by_role 'Parent'
  end

  # retrives the users in the family with 'Kid' role
  def kids
    members.by_role 'Kid'
  end
  
  def events
    Event.where(:phone_id => phones)
  end
  
  def alerts
    phones.map(&:alerts).flatten
  end
end
