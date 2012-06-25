class Event < ActiveRecord::Base
  self.primary_key = "idevents"

  include GeoHelper

  enum_attr :type, [:updated_GPS_position, :textbuster_discovered, :textbuster_lost], :plural=>:type_values
  enum_attr :gps_state, %w(^no_gps gps_off gps_on new_position), :plural=>:gps_state_values
  enum_attr :bt_state, %w(^no_bt bt_on bt_off), :plural=>:bt_state_values
  enum_attr :locked, %w(^not_locked locked), :plural=>:locked_values

  belongs_to :phone
  belongs_to :device, :foreign_key => 'device'

  [:type, :gps_state, :bt_state, :locked].each do |att|
    alias :"e#{att}=" :"#{att}="
    define_method "#{att}=" do |val|
      self.send(:"e#{att}=", self.send(:"#{att}_values")[val])
    end
  end

  def time= val
    created_at = Time.at(val)
  end

  def event_type= val
    type = self.enums(:type)[val]
  end

end

