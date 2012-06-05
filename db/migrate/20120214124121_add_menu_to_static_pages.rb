class AddMenuToStaticPages < ActiveRecord::Migration
  def self.up
    change_table :pages do |t|
      t.boolean :menu_logged_in
      t.boolean :menu_logged_out
      t.boolean :menu_footer
    end
  end

  def self.down
    remove_column :pages, :menu_logged_in
    remove_column :pages, :menu_logged_out
    remove_column :pages, :menu_footer
  end
end

