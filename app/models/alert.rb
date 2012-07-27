class Alert < ActiveRecord::Base
  has_and_belongs_to_many :phones
  has_and_belongs_to_many :users
  belongs_to :author, :class_name => "User"
  has_many :restrictions
  has_many :alert_trip_notifications
  has_many :alert_histories
  accepts_nested_attributes_for :restrictions, :allow_destroy => true, :reject_if => :all_blank
  
  enum_attr :event_type, ["^Leaving area", "Driving at a specific time","Speed restriction"], :nil => false
  
  validates :name, :event_type, :presence => true
  validates :restricted_time_start, :restricted_time_end, :format => /^(([0-9])|([0-1][0-9])|([2][0-3])):(([0-9])|([0-5][0-9])) (AM|PM)$/, :if => :time_type?
  validate :restrictions_present, :unless => :time_type?

  validate :validate_restricted_time, :restricted_time_end

  validates :speed, :numericality => true, :if => :speed_type?
  validate :valid_speed, :if => :speed_type?

  scope :time_resrtriction, where(:event_type => 'Driving at a specific time')
  scope :zone_resrtriction, where(:event_type => 'Leaving area')
  scope :speed_resrtriction, where(:event_type => 'Speed restriction')
  

  def self.min_speed
    Setting.find_by_name('alert_min_speed').value.to_i || 5
  end

  def conditions
    "Alert if " << if event_type.eql? :"Leaving area"
      "further away then " << (restrictions.map(&:to_s).join ", or ")
    elsif event_type.eql? :"Driving at a specific time"
       "driving from " << restricted_time_start << " till " << restricted_time_end
    elsif event_type.eql? :"Speed restriction"
      "speed exceeds #{speed} MPH, notify me when speed over #{speed_over} MPH"
    end
  end
  
  def time_errors
    errors[:restricted_time_start] || errors[:restricted_time_end]
  end
  
private

  def time_type?
    event_type.to_s =~ /specific time/
  end

  def speed_type?
    event_type.to_s =~ /Speed restriction/
  end
  
  def restrictions_present
    errors.add(:restrictions, "At least 1 restriction should be added") if event_type == :'Leaving area' && (restrictions.size.zero? || !restrictions.map(&:_destroy).include?(false))
  end

  def valid_speed
    errors.add(:speed, "must be greater than #{Alert.min_speed} and less than 200") if speed && !(Alert.min_speed..200).to_a.include?(speed.to_i)
  end

  def validate_restricted_time
    time_start = Time.parse(restricted_time_start)
    time_end = Time.parse(restricted_time_end)
    errors.add(:restricted_time_end, "must be greater than #{restricted_time_start}") if time_start && time_end && (time_start > time_end)
  end
end
