(ns noamb.foe.auth
  ^{:author "Noam Ben Ari"
    :doc "Functions for updating the session with the authentication state."}
  (:require [noamb.foe.user :refer [authenticate-from-storage]]))

(defn logged-in?
  "Takes a session map and inspects it for the required keys to be logged in.
   Returns true if logged in, false otherwise."
  [session]
  (boolean (session :user-id)))

(defn authenticate ; TODO: should loop over all authenticate options until authenticated
  [db identifiers]
  (authenticate-from-storage db identifiers))

(defn login-session
  "Sets user-id in session (effectively logs in the user)."
  [session user-id]
  (assoc session :user-id user-id))

(defn reset-session
  "Resets session by associng nil to the :session key in the response and thus activating the wrap-session middleware own reset session mechanism."
  [resp]
  (assoc resp :session nil))
