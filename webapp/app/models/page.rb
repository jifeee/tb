# static page class
class Page < ActiveRecord::Base
  validates :title, :presence => true, :uniqueness => true
  scope :by_menu, lambda{ |menu| where(menu => true) }

  # save a parameterized version of anchor
  before_save do |record|
    record.anchor = record.title.parameterize
  end

  # allow to refer to resource by slug instead of id
  def to_param
    anchor
  end
end

