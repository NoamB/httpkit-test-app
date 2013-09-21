(ns core
  {:author "Noam Ben Ari"}
  (:require [org.httpkit.server :refer [run-server]])
  (:require [clojure.tools.logging :refer [debug info error enabled?]])
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


(declare -main)

(def stop-server-fn (atom #())) ; declared as no-op Fn to prevent NPE.

(defn stop-server
  []
  (@stop-server-fn))

(defn reload
  []
  (stop-server)
  (clojure.tools.namespace.repl/refresh)
  (-main))

(defmacro bench
  "Returns a vector containing the result of form and the time it took to compute in msec."
  [level form]
  `(let [start# (java.lang.System/nanoTime)
         result# ~form
         total-time# (when (enabled? ~level)
                       (/ (double (- (java.lang.System/nanoTime) start#)) 1000000.0))]
     [result# total-time#]))

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

(defn wrap-logging [handler]
  (fn [req]
    (info "=== REQUEST STARTED ===>" (req :uri) \newline)
    (let [result (bench :debug (handler req))
          resp (first result)
          request-time (second result)]
      (info "Params:" (req :params))
      (info "Sent Response: " resp)
      (info (str  "<=== REQUEST ENDED === (" request-time " msec)") \newline)
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
                       wrap-lint
                       reload/wrap-reload) ;; only reload when dev
                  (site all-routes))]
    (if dev-mode
      (info "--- DEV MODE ---")
      (info "%%% PROD MODE %%%"))
    (let [stop-fn (run-server (-> handler wrap-logging) {:port 8080})]
      (reset! stop-server-fn stop-fn))
    ))
