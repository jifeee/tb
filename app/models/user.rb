# User model
class User < ActiveRecord::Base
  devise :database_authenticatable, :registerable, :recoverable, :trackable, :validatable, :rememberable

  attr_accessible :password, :password_confirmation, :restrictions_attributes, *(column_names rescue [])
  
  attr_accessor :pwd

  belongs_to :family
  belongs_to :role
  has_many :trips, :order => "created_at DESC"
  has_many :trips_without_order, :class_name => 'Trip'
  has_many :phones

  has_many :authored_alerts, :class_name => "Alert", :foreign_key => "author_id"
  has_and_belongs_to_many :alerts

  delegate :parents, :kids, :to => :family, :allow_nil => true
  delegate :permissions, :to => :role
  
  scope :by_role, lambda{|role_name| where(:role_id => Role.find_by_name(role_name))}
  scope :main, where(:is_main => true)

  before_create do |record| # fill the defaults if not set
    record.name ||= record.login
    record.role_id ||= Role.find_by_name('Parent').id
    record.is_main = true unless record.family_id
    record.build_family unless record.admin? || record.family_id
  end
  after_create :send_welcome_email
  # before_validation :generate_pwd, :on => :create, :if => "password.blank? && password_confirmation.blank?"

  after_validation :email_validation_only_one_message
  
  validates :email, :format =>  /^[a-z0-9,!#\$%&'\*\+\/=\?\^_`\{\|}~-]+(\.[a-z0-9,!#\$%&'\*\+\/=\?\^_`\{\|}~-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*\.([a-z]{2,})$/
  validates :email, :name, :presence => true
  
  # methods determining if the user belongs to a particular role
  # example:
  # User.first.admin?    #=> true
  # User.first.kid?      #=> false
  (Role.all rescue []).map(&:name).each do |r|
    define_method "#{r.downcase}?" do
      r.eql? role.try(:name)
    end
  end

  # get devices through the phones association
  def devices
    phones.map(&:devices).flatten.uniq
  end

  # override for Devise native method, in order to allow users
  # modify their accounts without providing a password
  def update_with_password params={}
    params.delete :current_password
    update_attributes(params)
  end

  # determines if the user is a parent for the particular kid
  def watches? kid
    parent? && kid.kid? && kid.family_id.eql?(family_id)
  end

  # reset authentication token
  def reset_authentication_token!
    update_attribute :authentication_token, SecureRandom.hex(16)
  end

  protected
  # send an email on registration
  def send_welcome_email
    #Mailer.registered(self).deliver
  end

  # generate temporary passwords if user was registered by the other user
  def generate_pwd
    pwd = Devise.friendly_token.first(10)
    self.password, self.password_confirmation = pwd, pwd
  end

  def email_validation_only_one_message
    self.errors[:email].clear && self.errors[:email] = ['is invalid'] if self.errors.has_key?(:email)
  end

end

