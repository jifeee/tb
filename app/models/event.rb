class Event < ActiveRecord::Base
  include GeoHelper

  set_inheritance_column :itsnotinheritanceclass

  enum_attr :type, [:updated_GPS_position, :textbuster_discovered, :textbuster_lost], :plural=>:type_values
  enum_attr :gps_state, %w(^no_gps gps_off gps_on new_position), :plural=>:gps_state_values
  enum_attr :bt_state, %w(^no_bt bt_on bt_off), :plural=>:bt_state_values
  enum_attr :locked, %w(^not_locked locked), :plural=>:locked_values

  belongs_to :location, :foreign_key => 'locations_id'
  belongs_to :device, :class_name => 'Device', :foreign_key => 'textbuster_mac', :primary_key => 'imei'
  belongs_to :phones_log
  has_one :phone, :through => :phones_log

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

  class << self
    def query_for_trips_calculation(textbuster_mac,phones_log_id)
      q = "
        select 
            qr2.id as 'qr2_id', qr2.next_id as 'qr2_next_id', 
            qr0.id as 'qr0_id', qr0.next_id as 'qr0_next_id'
        from
        (
        select 
         min(q.id) 'id',
         q.next_id
        from (select e.id, (select id from events where id>e.id and locked=0 
          and (textbuster_mac = '%%textbuster_mac%%' and phones_log_id = %%phones_log_id%%) 
        limit 1) as 'next_id'
        from events e where e.locked=2
        and (e.textbuster_mac = '%%textbuster_mac%%' and e.phones_log_id = %%phones_log_id%%)
        ) q
        where next_id is not null
        group by q.next_id
        ) qr2
        left join 
        (
        select 
         min(q.id) 'id',
         q.next_id
        from (select e.id, (select id from events where id>e.id and locked=2 and (textbuster_mac = '%%textbuster_mac%%' and phones_log_id = %%phones_log_id%%) limit 1) as 'next_id'
        from events e where e.locked=0
        and (e.textbuster_mac = '%%textbuster_mac%%' and e.phones_log_id = %%phones_log_id%%)
        ) q
        where next_id is not null
        group by q.next_id
        having 
         MINUTE(TIMEDIFF((select time from events where id=min(q.id) limit 1),(select time from events where id=q.next_id limit 1))) >= 10
        ) qr0 on qr2.next_id=qr0.id
      where 
        (select count(last_event_id) from calculated_events where textbuster_mac='%%textbuster_mac%%' and phones_log_id=%%phones_log_id%%) = 0
        or qr2.next_id > (select last_event_id from calculated_events where textbuster_mac='%%textbuster_mac%%' and phones_log_id=%%phones_log_id%%)
      ".gsub(/%%textbuster_mac%%/,textbuster_mac.to_s).gsub(/%%phones_log_id%%/,phones_log_id.to_s)
    end
  end

end

