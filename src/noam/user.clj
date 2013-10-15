(ns noam.user
  (:require [clj-bcrypt-wrapper.core :refer [encrypt gensalt check-password]]
            [clojure.tools.logging :refer [info error]]
            [noam.util.db :as db]))

(defrecord User [id username encrypted-password])

(defprotocol IUserStorage
  "IUserStorage docstring"
  (update-attributes! [this attrs-map] "docstring")
  (find-by-identifiers [this identifiers] "docstring"))

(deftype MySQLUserStorage
         []
  IUserStorage
  (update-attributes!
      [this attrs-map]
    (db/exec (db/build-query-from-attrs "UPDATE Users SET " attrs-map " WHERE id = ?")))
  (find-by-identifiers
      ^User
      [this identifiers]
    (let [ps (db/map-to-prepared-statement identifiers)
          query (str "SELECT * FROM Users WHERE " (first ps))
          params (second ps)
          query-and-params (cons query params)]
      (map->User (first (db/select query-and-params))))))

(defn- salt [] (gensalt 10))

(defn authenticate-from-storage
  "Looks in user storage for a user record with the supplied identifiers. If found returns the User, else nil."
  [db identifiers]
  (info identifiers)
  (let [user (.find-by-identifiers db (select-keys identifiers [:username]))]
    (info user)
    (when (and
            user
            (check-password
              (:password identifiers)
              (:encrypted-password user)))
      user)))
