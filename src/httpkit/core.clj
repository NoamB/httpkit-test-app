;;;; This module includes the main entry point of the app (-main)
;;;; as well as methods for building up the main app handler from
;;;; different middleware pieces in order.
;;;; This ns will also take care of constructing the app with the
;;;; right config.
(ns httpkit.core
  {:author "Noam Ben Ari"}
  (:require [clojure.tools.logging :refer [debug info error]]

            [ring.middleware.session :as session]
            [ring.middleware.json :refer [wrap-json-response]]

            [compojure.handler :refer [site]] ; form, query params decode; cookie; session, etc
            [noam.util.bench :refer [bench]]
            [noam.server :as server]
            [noam.controller :refer [all-routes]]
            [noamb.foe.user :refer :all]
            [noam.util.db :as db])
  (:import noam.util.db.MySQLUserStorage)
  (:gen-class))

(declare wrap-outer-logging)
(declare gen-handler)

(defn system []
  (let [user-storage (MySQLUserStorage.)]
    {:db {:user-storage user-storage}
     :server {:port 8080}}))

(defn start [system]
  (server/start! (gen-handler system) (:server system))
  system)

(defn stop [system]
  (server/stop!))

(defn wrap-outer-logging [handler]
  (fn [req]
    (info "=== REQUEST STARTED ===>" (req :uri))
    (let [result (bench :info (handler req))
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

(defn- gen-handler
  [system]
  (-> system
      all-routes ; main router
      wrap-inner-logging ; inner logging
      wrap-json-response ; turns clojure to json response
      site ; session, flash and more...
      wrap-outer-logging)) ; outer logging

(defn -main
  "Starts the app"
  [& args]
  ; work around dangerous default behaviour in Clojure
  ; (alter-var-root #'*read-eval* (constantly false))
  (info "starting up ...")
  (info "args: " args)
  (set! *warn-on-reflection* true)

  (start (system)))
