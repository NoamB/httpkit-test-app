(ns core
  {:author "Noam Ben Ari"}
  (:require [org.httpkit.server :refer [run-server]])
  (:require [clojure.tools.logging :refer [info error]])
  (:require [ring.middleware.reload :as reload]
            [ring.middleware.session :as session]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.util.response :refer [response redirect]]
            [compojure.response :refer [render]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route :refer [resources files not-found]]
            [compojure.handler :refer [site]] ; form, query params decode; cookie; session, etc
            [cheshire.core :as json :refer [generate-string]]
            [noam.auth :refer :all]
            [noam.user :refer :all])
  (:gen-class))

(defn- not-authenticated
  []
  {:status 403
   :headers {}
   :body "wrong credentials!" })

(defn main-page [{session :session :as req}]
  (if (logged-in? session)
    (render "Welcome! <a href=\"/logout\">Logout</a>" req)
    (redirect "/login.html"))
  )

(defn create-login [{session :session params :form-params}]
  (info params)
  (if-let [user (authenticate [(params "username") (params "password")])] ;; TODO: sanitize user data?
    (let [session (assoc session :user-id (.id user))]
      (-> (redirect "/")
          (assoc :session session)))
    (not-authenticated)))

(defn logout
  [req]
  (-> (redirect "/")
      (assoc :session nil)))

(defn wrap-logging [handler]
  (fn [req]
    (info "=== REQUEST STARTED ===>" (req :uri) \newline req)
    (let [resp (handler req)]
      (info "Sent Response: " \newline resp)
      (info "<=== REQUEST ENDED ===" \newline)
      resp)))

(defroutes all-routes
  (GET "/" [] main-page)
  (POST "/login" [] create-login)
  (GET "/logout" [] logout)
  (route/files "/") ;; static file url prefix /, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(defn in-dev?
  [args]
  (some #(= "dev" %) args))

(defn -main
  "Starts the app"
  [& args]
  ;; work around dangerous default behaviour in Clojure
  ;;(alter-var-root #'*read-eval* (constantly false))
  (info "starting up ...")
  (info "args: " args)
  (let [dev-mode (in-dev? args)
        handler (if dev-mode
                  (->  (site all-routes)
                       reload/wrap-reload
                       wrap-lint) ;; only reload when dev
                  (site all-routes))]
    (if dev-mode
      (info "--- DEV MODE ---")
      (info "%%% PROD MODE %%%"))
    (run-server (-> handler wrap-logging) {:port 8080})))
