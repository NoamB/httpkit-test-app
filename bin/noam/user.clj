(ns noam.user
  (:require [clj-bcrypt-wrapper.core :refer [encrypt gensalt check-password]]
            [clojure.tools.logging :refer [info error]]
            [noam.util.db :as db]))

(defrecord User [id username encrypted-password])

(defprotocol IUserStorage
  "IUserStorage docstring"
  (update-attributes [this attrs-map] "docstring")
  (find-by-identifiers [this identifiers] "docstring"))

(deftype MySQLUserStorage
         []
  IUserStorage
  (update-attributes
      [this attrs-map]
    (db/exec (db/build-query-from-attrs "UPDATE Users SET " attrs-map " WHERE id = ?")))
  (find-by-identifiers
      ^User
      [this identifiers]
    (map->User (first
                 (db/select ["SELECT * FROM Users WHERE username = ? AND id = ?", "ars3", 1])))))

(defn salt [] (gensalt 10))

(defn authenticate-from-storage
  "Looks in user storage for a user record with the supplied identifiers. If found returns the User, else nil."
  [identifiers]
  (.find-by-identifiers (->MySQLUserStorage) identifiers))
