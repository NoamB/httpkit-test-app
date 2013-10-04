;;;; small http-kit wrapper exposing the server as a 'component'.
(ns noam.server
  (:require [org.httpkit.server :refer [run-server]]))

(def ^:private stop-server-fn (atom nil))

(defn stop!
  []
  (when-not (nil? @stop-server-fn)
    (prn "stopping server...")
    (@stop-server-fn)
    (prn "...stopped.")
    (reset! stop-server-fn nil)))

(defn start!
  [handler]
  (let [stop-fn (run-server handler {:port 8080})]
    (reset! stop-server-fn stop-fn)))
