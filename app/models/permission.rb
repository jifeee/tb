include AppResources

# Each permission for the Role
class Permission < ActiveRecord::Base
  has_and_belongs_to_many :roles
  
  def self.possible_permissions
    Rails.cache.write(:permissions, AppResources::possible_permissions) unless Rails.cache.read(:permissions)
    Rails.cache.read(:permissions)
  end
end

