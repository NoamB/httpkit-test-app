(ns core
  {:author "Noam Ben Ari"}
  (:require [org.httpkit.server :refer [run-server]])
  (:require [clojure.tools.logging :refer [debug info error enabled?]])
  (:require [ring.middleware.reload :as reload]
            [ring.middleware.session :as session]
            [ring.middleware.lint :refer [wrap-lint]]
            [ring.util.response :refer [response redirect]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [compojure.response :refer [render]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route :refer [resources files not-found]]
            [compojure.handler :refer [site]] ; form, query params decode; cookie; session, etc
            [cheshire.core :as json :refer [generate-string]]
            [noam.auth :refer :all]
            [noam.user :refer :all])
  (:gen-class))

; declarations
(declare index)
(declare login)
(declare logout)
(declare -main)

; dev utils
(def stop-server-fn (atom nil))

(defn stop-server
  []
  (when (not (nil? @stop-server-fn))
    (do
      (prn "stopping server...")
      (@stop-server-fn)
      (prn "...stopped.")
      (reset! stop-server-fn nil))))

(defn start-server!
  [handler]
  (let [stop-fn (run-server handler {:port 8080})]
    (reset! stop-server-fn stop-fn)))

(defn reload
  []
  (stop-server)
  (refresh)
  (prn "starting server...")
  (-main '("dev")))

(defroutes all-routes
  (GET "/" [] index)
  (POST "/login" [] login)
  (GET "/logout" [] logout)
  (route/files "/") ; static file url prefix /, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ; all other, return 404

; bench
(defmacro bench
  "Returns a vector containing the result of form and the time it took to compute in msec."
  [level form]
  `(let [start# (java.lang.System/nanoTime)
         result# ~form
         total-time# (when (enabled? ~level)
                       (/ (double (- (java.lang.System/nanoTime) start#)) 1000000.0))]
     [result# total-time#]))

; controller
(defn- not-authenticated
  []
  {:status 403
   :headers {}
   :body "wrong credentials!" })

(defn index [{session :session :as req}]
  (if (logged-in? session)
    (render "Welcome! <a href=\"/logout\">Logout</a>" req)
    (redirect "/login.html"))
  )

(defn login [{session :session params :form-params}]
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

; server
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

(defn in-dev?
  [args]
  (some #(= "dev" %) args))

(defn gen-prod-handler
  []
  (site all-routes))

(defn gen-dev-handler
  []
  (-> (gen-prod-handler)
      wrap-lint
      reload/wrap-reload))

(defn get-handler
  [dev-mode?]
  (if dev-mode?
    (gen-dev-handler)
    (gen-prod-handler)))

(defn -main
  "Starts the app"
  [& args]
  ; work around dangerous default behaviour in Clojure
  ; (alter-var-root #'*read-eval* (constantly false))
  (info "starting up ...")
  (info "args: " args)
  (let [dev-mode (in-dev? args)
        handler (get-handler dev-mode)]
    (if dev-mode
      (info "---><><>< DEV  MODE ><><><---")
      (info "%%%%%%%%% PROD MODE %%%%%%%%%"))
    (start-server! (-> handler wrap-logging))))
