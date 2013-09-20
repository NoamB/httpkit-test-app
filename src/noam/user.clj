(ns noam.user
  (:require [clj-bcrypt-wrapper.core :refer :all]
            [clojure.tools.logging :refer [info error]]))

(defrecord User [id username encrypted-password])

(def salt (gensalt 10)) ;; will need to change when DB is added

;; go to storage
(defn- getUser [username encrypted-password]
  (info encrypted-password)
  (if (and (= username "noam")
           (= encrypted-password (encrypt salt "1234")))
    (->User 1 username encrypted-password)
    nil))

(defn authenticate
  "Looks in user storage for a user record with the supplied credentials. If found returns the User, else nil."
  [credentials]
  (getUser (first credentials) (encrypt salt (second credentials))))
