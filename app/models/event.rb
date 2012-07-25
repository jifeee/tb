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
  has_many :alert_histories

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
    def events_for_calculation_trip(textbuster_mac,phones_log_id)
      q = "
      select min(q3.start_time) start_time, max(q3.end_time) end_time
      from
      (
      select q2.*,
      if (q2.duration >= 10*60 and q2.locked=0, @v2:=@v2+1,0) c2,
      if (TIME_TO_SEC(TIMEDIFF(q2.start_time,@old_time)) >= 10*60 and q2.locked=2, @v2:=@v2+1, 0) c3,
      @old_time:=q2.end_time,
      @v2 as v2
      from
      (
      select 
      q.v,
      q.locked,
      TIME_TO_SEC(TIMEDIFF(max(q.time),min(q.time))) duration,
      min(q.time) start_time,
      max(q.time) end_time
      from
      (
      select
      e.*,
      IF (e.locked = @old_locked, 0, @v:=@v+1) if1,
      IF (e.locked = @old_locked, 0, @old_locked:=e.locked) if2,
      IF (TIME_TO_SEC(TIMEDIFF(e.time,@old_time1)) >= 10*60, @v:=@v+1,0) if3,
      @old_time1:=e.time,
      @v as v
      from 
      events e
      where 
      textbuster_mac = '%%textbuster_mac%%' and phones_log_id = %%phones_log_id%%
      and ((@last_time is null) or (e.time > @last_time))
      ) q
      group by q.v, q.locked
      ) q2
      ) q3
      where c2=0 
      group by q3.v2".gsub(/%%textbuster_mac%%/,textbuster_mac.to_s.to_mac).gsub(/%%phones_log_id%%/,phones_log_id.to_s)

      events = ActiveRecord::Base.transaction do
        a = ActiveRecord::Base.connection.execute("select @last_time:=last_time from calculated_events 
          where 
            textbuster_mac = '%%textbuster_mac%%' and phones_log_id = %%phones_log_id%%
          ".gsub(/%%textbuster_mac%%/,textbuster_mac.to_s.to_mac).gsub(/%%phones_log_id%%/,phones_log_id.to_s)
        )
puts ">>>> #{a.inspect}"        
        %w(v2 old_time old_time1 old_locked v).map do |var|
          ActiveRecord::Base.connection.execute("SET @#{var} = 0;")
        end
        ActiveRecord::Base.connection.select_all(q)
      end
  puts "************** #{events}"

      return events
    end
  end

end

