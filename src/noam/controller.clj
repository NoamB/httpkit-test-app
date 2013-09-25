(ns noam.controller
  (:require [clojure.tools.logging :refer [debug info error]]
            [ring.util.response :refer [response redirect]]
            [compojure.response :refer [render]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route :refer [resources files not-found]]
            [cheshire.core :as json :refer [generate-string]]
            [noam.auth :refer :all]
            [noam.user :refer :all]
))

(defn not-authenticated
  []
  {:status 403
   :headers {}
   :body "wrong credentials!" })

(defn index [{session :session :as req}]
  (if (logged-in? session)
    (render "Welcome! <a href=\"/logout\">Logout</a>" req)
    (redirect "/login.html"))
  )

(defn login [{session :session params :form-params :as req}]
  (if-let [user (authenticate [(params "username")
                               (params "password")])] ;; TODO: sanitize user data?
    (let [session (login-session session (.id user))]
      (-> (redirect "/")
          (assoc :session session)))
    (not-authenticated)))

(defn logout
  [req]
  (-> (redirect "/")
      (reset-session)))

(defroutes all-routes
  (GET "/" [] index)
  (POST "/login" [] login)
  (GET "/logout" [] logout)
  (route/files "/") ; static file url prefix /, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ; all other, return 404
