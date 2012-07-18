class String
  def to_mac
    self.gsub(/[^\w\d$]/,'').upcase.split(/(.{2})/).reject {|s| s.blank? }.join(':')
  end
end