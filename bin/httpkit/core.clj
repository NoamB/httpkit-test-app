;;;; This module includes the main entry point of the app (-main)
;;;; as well as methods for building up the main app handler from
;;;; different middleware pieces in order.
(ns httpkit.core
  {:author "Noam Ben Ari"}
  (:require [clojure.tools.logging :refer [debug info error]]

            [ring.middleware.session :as session]
            [ring.middleware.json :refer [wrap-json-response]]

            [compojure.handler :refer [site]] ; form, query params decode; cookie; session, etc
            [noam.util.bench :refer [bench]]
            [noam.server :as server]
            [noam.controller :refer [all-routes]])
  (:import noam.user.MySQLUserStorage)
  (:gen-class))

(declare wrap-outer-logging)
(declare get-handler)

(defn system []
  {:handler      (-> (get-handler true) wrap-outer-logging)
   :user-storage (MySQLUserStorage.)})

(defn start [sys]
  (server/start! (:handler sys)))

(defn stop [sys]
  (server/stop!))

(defn wrap-outer-logging [handler]
  (fn [req]
    (info "=== REQUEST STARTED ===>" (req :uri))
    (let [result (bench :debug (handler req))
          resp (first result)
          request-time (second result)]
      (info "Sent Response: " resp)
      (info (str "<=== REQUEST ENDED === "
                 (when request-time
                   (str "(" request-time " msec)")) \newline))
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
              wrap-inner-logging
              wrap-json-response))))

(defn- gen-dev-handler
  []
  (-> (gen-prod-handler)))

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
  (set! *warn-on-reflection* true)
  (let [dev-mode (in-dev? args)
        handler (get-handler dev-mode)]
    (if dev-mode
      (info "---><><>< DEV  MODE ><><><---")
      (info "%%%%%%%%% PROD MODE %%%%%%%%%"))
    (server/start! (-> handler wrap-outer-logging))))
