class Phone < ActiveRecord::Base
  has_and_belongs_to_many :devices
  has_and_belongs_to_many :alerts
  
  has_many :events, :through => :phones_log
  has_many :phones_log, :foreign_key => 'imei', :primary_key => 'imei'

  belongs_to :user

  scope :event_phones, :joins => [:phones_log], :select => 'phones_log.*'

  ABILITIES = [:GPS, :Bluetooth]

  validates :imei, :presence => true
  
  bitmask :abilities, :as => ABILITIES
  
  def last_position
    events.where("locations_id is not null").order("created desc").first
  end
  
  def restrictions
    alerts.map(&:restrictions)
  end
end
