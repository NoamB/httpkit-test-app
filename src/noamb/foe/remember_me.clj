(ns noamb.foe.remember-me
  ^{:author "Noam Ben Ari"
    :doc "This module handles authentication using cookies."}
  (:require [noamb.foe :as foe]))

(defn authenticate-from-cookie
  [db identifiers]
  (prn "cookie auth called")
  (when (:remember-me identifiers)
    (prn "should remember!")))

(defn start!
  []
  (prn "remember-me module activated")
  (swap! foe/authentication-methods #(into [authenticate-from-cookie] %)))

(defn remember-me
  "Sets a remember-me cookie."
  [resp]
  ;; TODO
  )

(defn forget-me
  "Removes the remember-me cookie."
  [resp]
  ;; TODO
  )
