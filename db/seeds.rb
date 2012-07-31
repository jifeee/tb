# This file should contain all the record creation needed to seed the database with its default values.
# The data can then be loaded with the rake db:seed (or created alongside the db with db:setup).
#
# Examples:
#
#   cities = City.create([{ :name => 'Chicago' }, { :name => 'Copenhagen' }])
#   Mayor.create(:name => 'Daley', :city => cities.first)

admin = Role.create :name => 'Admin'
Role.create :name => 'Parent'
Role.create :name => 'Kid'

load 'app/models/user.rb' # reload class to get roles
User.create :email => 'admin@admin.com',
            :login => 'admin',
            :name => 'admin',
            :password => 'qwe123',
            :password_confirmation => 'qwe123',
            :role_id => admin.id

Setting.create(:name => :sms_username, :value => '')
Setting.create(:name => :sms_password, :value => '')
Setting.create(:name => :mobile_format, :value => '')
Setting.create(:name => :max_parents, :value => 10)
Setting.create(:name => :max_kids, :value => 10)


[{:resource =>'Phone', :menu_item => 'TextBuster Phone'},
	{:resource => 'Device', :menu_item => 'TextBuster Device'},
	{:resource => 'Event', :menu_item => 'View Events'},
	{:resource => 'Trip', :menu_item => 'View Trips'},
	{:resource => 'Alert', :menu_item => 'Set Alerts'},
	{:resource => 'Family', :menu_item => 'Account Managers'}].map {|r| MenuItem.create r}

