(ns noamb.foe.user
  ^{:author "Noam Ben Ari"
    :doc "This module handles authenticating using user-supplied credentials."}
  (:require [clj-bcrypt-wrapper.core :as bcrypt]
            [clojure.tools.logging :refer [info error]]
            [noamb.foe :as foe]))

(defprotocol IUserStorage
  "A generic interface for your user storage, be it in RDBMS, NoSQL or another storage engine.
  Defines functions for retrieving and updating a user."
  (create! [this attrs-map] "create a user record using a map of attributes. Should return the new user.")
  (update! [this attrs-map] "updates a user record using a map of attributes. Should return an updated user.")
  (find-user [this identifiers] "returns a user record using an identifiers map (for example {:username 'sam'})."))

(defn- salt [] (bcrypt/gensalt 10))

(defn encrypt-password [clear-password]
  (bcrypt/encrypt (salt) clear-password))

(defn authenticate-from-user
  "Looks in user storage for a user record with the supplied identifiers. If found returns the User, else nil."
  [db identifiers]
  (info (str identifiers))
  (let [user (find-user db (select-keys identifiers [:username]))]
    (when (and
            user
            (bcrypt/check-password
              (:password identifiers)
              (:encrypted-password user)))
      user)))

(defn start!
  []
  (swap! foe/authentication-methods #(into [authenticate-from-user] %)))
