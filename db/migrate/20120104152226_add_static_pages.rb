class AddStaticPages < ActiveRecord::Migration
  def self.up
    create_table :pages do |t|
      t.string :title
      t.string :anchor
      t.text   :data
    end
  end

  def self.down
    drop_table :pages
  end
end
