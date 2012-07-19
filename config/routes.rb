Textbuster::Application.routes.draw do
  match "pages/media/:type/:file_name" => "media#show"

  ActiveAdmin.routes(self)

  # devise_for :users, :only => [:registrations, :passwords]
  devise_for :users, :only => [:registrations, :passwords, :unlocks], :controllers => {:passwords => "passwords"}
  
  resources :passwords do
    post 'reset' => 'passwords', :on => :collection
  end

  devise_for :users, ActiveAdmin::Devise.config

  resources :alerts do
    get 'alerts/delete' => "alerts#batch_delete", :on => :collection, :as => :delete
  end
  resources :textbusters, :controller => "devices" do
    get 'textbusters/delete' => "devices#batch_delete", :on => :collection, :as => :delete
  end
  resources :events, :only => :index
  resources :families, :except => [:new, :create, :destroy] do
    resources :kids
  end
  resources :parents, :only => [:new, :create]
  resources :pages, :only => :show
  resources :trips, :only => [:index, :show, :destroy]
  resource :user do
    resources :restrictions
  end
  resources :phones, :except => [:new, :create] do
    get 'phones/delete' => "phones#batch_delete", :on => :collection, :as => :delete
  end
  
  namespace :api do
    post 'login' => 'users#login' #post
    post 'sign_up' => 'users#sign_up' #post
    post 'change_password' => 'users#change_password' #post
    post 'get_textbusters/:imei' => 'users#get_textbusters'    
    post 'textbusters/create/:imei/:mac/:token' => 'textbusters#create'   

    post 'system_notification/:id' => 'users#system_notification'
    
    resources :trips
  end
  
  root :to => "application#main"
end
