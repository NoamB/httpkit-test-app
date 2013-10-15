(ns noam.controller
  (:require [clojure.tools.logging :refer [debug info error]]
            [ring.util.response :refer [response redirect]]
            [compojure.response :refer [render]]
            [compojure.core :refer [routes GET POST]]
            [compojure.route :as route :refer [resources files not-found]]
            [noam.auth :refer [logged-in? authenticate login-session reset-session]]
            [noam.user :refer :all])
  (:import noam.user.MySQLUserStorage))

(defn not-authenticated
  []
  {:status 403
   :headers {}
   :body "wrong credentials!"})

(defn index [{session :session :as req}
             subsystem]
  (if (logged-in? session)
    (let [user (find-by-identifiers (:user-storage subsystem) {:id (session :user-id)})]
      (render (str "Welcome " (:username user) "! <a href=\"/logout\">Logout</a>") req))
    (redirect "/login.html")))

(defn login [{session :session params :form-params :as req}
             {db :user-storage :as subsystem}]
  (if-let [user (authenticate db {:username (params "username")
                                  :password (params "password")})] ;; TODO: sanitize user data?
    (let [session (login-session session (:id user))]
      (assoc (redirect "/") :session session))
    (not-authenticated)))

(defn logout
  [req]
  (reset-session (redirect "/")))

(defn myjson
  [req id]
  (response {:id id}))

(defn all-routes
  [system]
  (routes
    (GET "/" [] #(index % (:db system)))
    (POST "/login" [] #(login % (:db system)))
    (GET "/logout" [] logout)
    (GET "/myjson/:id" [id] #(myjson % id))
    (route/files "/") ; static file url prefix /, in `public` folder
    (route/not-found "<p>Page not found.</p>"))) ; all other, return 404
