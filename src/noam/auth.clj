(ns noam.auth
  {:author "Noam Ben Ari"})

(defn logged-in?
  "Takes a session map and inspects it for the required keys to be logged in.
   Returns true if logged in, else false."
  [session]
  (not (nil? (session :user-id))))

;; use defrecord for User.
;; maybe implement an interface
