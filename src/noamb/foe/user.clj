(ns noamb.foe.user
  (:require [clj-bcrypt-wrapper.core :refer [encrypt gensalt check-password]]
            [clojure.tools.logging :refer [info error]]))

(defrecord User [id username encrypted-password])

(defprotocol IUserStorage
  "IUserStorage docstring"
  (update! [this attrs-map] "docstring")
  (find-user [this identifiers] "docstring"))

(defn- salt [] (gensalt 10))

(defn authenticate-from-storage
  "Looks in user storage for a user record with the supplied identifiers. If found returns the User, else nil."
  [db identifiers]
  (info identifiers)
  (let [user (find-user db (select-keys identifiers [:username]))]
    (info user)
    (when (and
            user
            (check-password
              (:password identifiers)
              (:encrypted-password user)))
      user)))
