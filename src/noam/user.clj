(ns noam.user
  (:require [clj-bcrypt-wrapper.core :refer [encrypt gensalt check-password]]
            [clojure.tools.logging :refer [info error]]))

(defrecord User [id username encrypted-password])

(defprotocol IUserStorage
  "IUserStorage docstring"
  (update-attributes [this attrs-map] "docstring")
  (find-by-identifiers [this identifiers] "docstring"))

(defn salt [] (gensalt 10))

;; go to storage
(defn- getUser [username password]
  (if (and (= username "noam")
           (check-password "1234" (encrypt (salt) "1234")))
    (->User 1 username password)
    nil))

(defn authenticate-from-storage
  "Looks in user storage for a user record with the supplied identifiers. If found returns the User, else nil."
  [identifiers]
  (getUser (first identifiers) (second identifiers)))
