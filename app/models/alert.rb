class Alert < ActiveRecord::Base

  has_and_belongs_to_many :phones
  has_and_belongs_to_many :users
  belongs_to :author, :class_name => "User"
  has_many :restrictions
  has_many :alert_trip_notifications
  accepts_nested_attributes_for :restrictions, :allow_destroy => true, :reject_if => :all_blank
  
  enum_attr :event_type, ["^Leaving area", "Driving at a specific time"], :nil => false
  
  validates :name, :event_type, :presence => true
  validates :restricted_time_start, :restricted_time_end, :format => /^(([0-9])|([0-1][0-9])|([2][0-3])):(([0-9])|([0-5][0-9]))$/, :if => :time_type?
  validate :restrictions_present, :unless => :time_type?
  
  def conditions
    "Alert if " << if event_type.eql? :"Leaving area"
      "further away then " << (restrictions.map(&:to_s).join ", or ")
    else
      "driving from " << restricted_time_start << " till " << restricted_time_end
    end
  end
  
  def time_errors
    errors[:restricted_time_start] || errors[:restricted_time_end]
  end
  
  private
  def time_type?
    event_type.to_s =~ /specific time/
  end
  
  def restrictions_present
    errors.add(:restrictions, "At least 1 restriction should be added") if restrictions.size.zero? || !restrictions.map(&:_destroy).include?(false)
  end
end
