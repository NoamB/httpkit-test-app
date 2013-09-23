(ns core
  {:author "Noam Ben Ari"}
  (:require [clojure.tools.logging :refer [debug info error enabled?]])
  (:require [ring.middleware.reload :as reload]
            [ring.middleware.session :as session]
            [ring.middleware.lint :refer [wrap-lint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [compojure.handler :refer [site]] ; form, query params decode; cookie; session, etc

            [noam.util.bench :refer [bench]]
            [noam.server :as server]
            [noam.controller :refer [all-routes]])
  (:gen-class))

(declare -main)

(defn reload
  []
  (server/stop!)
  (refresh)
  (prn "starting server...")
  (-main '("dev")))

(defn wrap-outer-logging [handler]
  (fn [req]
    (info "=== REQUEST STARTED ===>" (req :uri) \newline)
    (let [result (bench :debug (handler req))
          resp (first result)
          request-time (second result)]
      (info "Sent Response: " resp)
      (info (str  "<=== REQUEST ENDED === (" request-time " msec)") \newline)
      resp)))

(defn wrap-inner-logging [handler]
  (fn [req]
    (do
      (info "Params:" (req :params))
      (handler req))))

(defn- in-dev?
  [args]
  (some #(= "dev" %) args))

(defn- gen-prod-handler
  []
  (->
   (site (-> all-routes
             wrap-inner-logging))))

(defn- gen-dev-handler
  []
  (-> (gen-prod-handler)
      wrap-lint
      reload/wrap-reload))

(defn- get-handler
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
    (server/start! (-> handler wrap-outer-logging))))
