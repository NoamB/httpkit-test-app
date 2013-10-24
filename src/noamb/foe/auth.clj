(ns noamb.foe.auth
  ^{:author "Noam Ben Ari"
    :doc "Functions for updating the session with the authentication state."}
  (:require [ring.util.response :refer [redirect]]
            [noamb.foe :as foe]))

(defn logged-in?
  "Takes a session map and inspects it for the required keys to be logged in.
   Returns true if logged in, false otherwise."
  [session]
  (boolean (session :user-id)))

(defn authenticate
  "Loops over all authentication methods until authenticated or all methods return nil."
  [db identifiers]
  (loop [methods @foe/authentication-methods]
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

(defn require-login*
  "Used to wrap a route handler, checks if 'logged-in?'. If true, allows the handler to run.
  If false, rejects the user."
  [success-method fail-method {session :session :as req}]
  (if (logged-in? session)
    (apply success-method [req])
    (apply fail-method [req])))

(defmacro require-login
  [fail-method form]
  (let [[method path args & body] form]
    `(~method ~path ~args #(require-login* ~@body ~fail-method %))))
