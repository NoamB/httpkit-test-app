(ns noam.controller
  (:require [clojure.tools.logging :refer [debug info error]]
            [ring.util.response :refer [response redirect]]
            [compojure.response :refer [render]]
            [compojure.core :refer [routes GET POST]]
            [compojure.route :as route :refer [resources files not-found]]
            [noamb.foe.auth :refer [logged-in? authenticate login logout require-login redirect-to-destination-or]]
            [noamb.foe.user :refer [find-user create!]]))

(defn wrong-credentials
  []
  {:status 403
   :headers {}
   :body "wrong credentials!"})

(defn not-authenticated
  [req & args]
  (assoc (redirect "/login.html") :flash "Please login."))

(defn index [{session :session :as req}
             {user-storage :user-storage :as subsystem}]
  (let [identifiers {:id (session :user-id)}
        user (find-user user-storage identifiers)]
    (render (str "Welcome " (:username user) "! <a href=\"/logout\">Logout</a>") req)))

(defn create-session [{session :session
                       params :form-params :as req}
                      {db :user-storage :as subsystem}]
  (if-let [user (authenticate db {:username (params "username")
                                  :password (params "password")
                                  :remember-me (params "remember-me")})] ; TODO: sanitize user data?
    (let [session (login session (:id user))]
      (redirect-to-destination-or "/" session))
    (wrong-credentials)))

(defn create-user [{session :session
                    params :form-params :as req}
                   {db :user-storage :as subsystem}]
  (if-let [user (create! db {:username (params "username")
                             :password (params "password")})]
    (let [session (login session (:id user))]
      (assoc (redirect "/") :session session))
    (wrong-credentials)))

(defn forgot-password [{session :session
                        params :form-params :as req}
                       {db :user-storage :as subsystem}]
  ; FIXME
  (wrong-credentials))

(defn destroy-session
  [req]
  (logout (redirect "/")))

(defn myjson
  [req id]
  (response {:id id}))

(defn all-routes
  [system]
  (routes
    (require-login not-authenticated
                   (GET "/" [] #(index % (:foe-config system)))
                   (GET "/logout" [] destroy-session)
                   (GET "/myjson/:id" [id] #(myjson % id)))

    (POST "/login" [] #(create-session % (:foe-config system)))
    (POST "/signup" [] #(create-user % (:foe-config system)))
    (POST "/forgot" [] #(forgot-password % (:foe-config system)))
    (route/files "/") ; static file url prefix /, in `public` folder
    (route/not-found "<p>Page not found.</p>"))) ; all other, return 404
