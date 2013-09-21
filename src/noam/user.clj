(ns noam.user
  (:require [clj-bcrypt-wrapper.core :refer [encrypt gensalt]]
            [clojure.tools.logging :refer [info error]]))

(defrecord User [id username encrypted-password])

(def salt (gensalt 10)) ;; will need to change when DB is added

;; go to storage
(defn- getUser [username encrypted-password]
  (if (and (= username "noam")
           (= encrypted-password (encrypt salt "1234")))
    (->User 1 username encrypted-password)
    nil))

(defn authenticate-from-storage
  "Looks in user storage for a user record with the supplied identifiers. If found returns the User, else nil."
  [identifiers]
  (getUser (first identifiers) (encrypt salt (second identifiers))))
