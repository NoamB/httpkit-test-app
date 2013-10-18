(ns noamb.foe.auth
  ^{:author "Noam Ben Ari"
    :doc "Functions for updating the session with the authentication state."}
  (:require [noamb.foe.user :refer [authenticate-from-storage]]))

(defn authenticate-from-cookie
  [db identifiers]
  nil)

(def ^{:dynamic true} *authentication-methods* [authenticate-from-cookie authenticate-from-storage])

(defn logged-in?
  "Takes a session map and inspects it for the required keys to be logged in.
   Returns true if logged in, false otherwise."
  [session]
  (boolean (session :user-id)))

(defn authenticate
  "Loops over all authentication methods until authenticated or all methods return nil."
  [db identifiers]
  (loop [methods *authentication-methods*]
    (when (seq methods)
      (or ((first methods) db identifiers)
          (recur (rest methods))))))

(defn login
  "Sets user-id in session (effectively logs in the user)."
  [session user-id]
  (assoc session :user-id user-id))

(defn logout
  "Resets session by associng nil to the :session key in the response and thus activating the wrap-session middleware own reset session mechanism."
  [resp]
  (assoc resp :session nil))
