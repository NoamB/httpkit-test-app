(ns noam.auth
  {:author "Noam Ben Ari"}
  (:require [noam.user :refer [authenticate-from-storage]]))

(defn logged-in?
  "Takes a session map and inspects it for the required keys to be logged in.
   Returns true if logged in, else false."
  [session]
  (not (nil? (session :user-id))))

(defn authenticate ;; should loop over all authenticate options
  [identifiers]
  (authenticate-from-storage identifiers))
