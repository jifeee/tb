# Represents user role
class Role < ActiveRecord::Base
  has_many :users
  has_and_belongs_to_many :permissions
  
  scope :by_name, lambda{|name| where(:name => name.to_s.downcase)}
  
  def admin?
    name == 'Admin'
  end
end
