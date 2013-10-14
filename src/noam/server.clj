;;;; small http-kit wrapper exposing the server as a 'component'.
(ns noam.server
  (:require [org.httpkit.server :refer [run-server]]))

(def
  ^{:doc "Holds the current stop-server-fn."
    :private true}
  stop-server-fn (atom nil))

(defn stop!
  []
  (when-not (nil? @stop-server-fn)
    (prn "stopping server...")
    (@stop-server-fn)
    (prn "...stopped.")
    (reset! stop-server-fn nil)))

(defn start!
  [handler subsystem]
  (let [stop-fn (run-server handler subsystem)]
    (reset! stop-server-fn stop-fn)))
