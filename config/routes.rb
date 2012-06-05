Textbuster::Application.routes.draw do
  ActiveAdmin.routes(self)
  
  devise_for :users, :only => [:registrations, :passwords]
  devise_for :users, ActiveAdmin::Devise.config

  resources :alerts do
    get 'alerts/delete' => "alerts#batch_delete", :on => :collection, :as => :delete
  end
  resources :textbusters, :controller => "devices" do
    get 'textbusters/delete' => "devices#batch_delete", :on => :collection, :as => :delete
  end
  resources :events, :only => :index
  resources :families, :except => [:new, :create, :destroy]
  resources :parents, :only => [:new, :create]
  resources :pages, :only => :show
  resources :trips, :only => [:index, :show]
  resource :user do
    resources :restrictions
  end
  resources :phones, :except => [:new, :create] do
    get 'phones/delete' => "phones#batch_delete", :on => :collection, :as => :delete
  end
  
  namespace :api do
    get 'login' => 'users#login' #POST
    get 'sign_up' => 'users#sign_up' #POST
    
    resources :trips
  end
  
  root :to => "application#main"
end
