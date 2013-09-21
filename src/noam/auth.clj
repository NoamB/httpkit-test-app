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

(defn authenticate-from-cookie
  []
  ;; TODO
  )

(defn login-session
  "Sets user-id in session (effectively logs in the user)."
  [session user-id]
  (assoc session :user-id user-id))


(defn reset-session
  "Resets session by associng nil to the :session key in the response and thus activating the wrap-session middleware own reset session mechanism."
  [resp]
  (assoc resp :session nil))

(defn remember-me
  "Sets a remember-me cookie."
  []
  ;; TODO
  )

(defn forget-me
  "Removes the remember-me cookie."
  []
  ;; TODO
  )
