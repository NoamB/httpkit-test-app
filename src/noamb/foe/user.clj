(ns noamb.foe.user
  ^{:author "Noam Ben Ari"
    :doc "This module handles authenticating using user-supplied credentials."}
  (:require [clj-bcrypt-wrapper.core :refer [encrypt gensalt check-password]]
            [clojure.tools.logging :refer [info error]]
            [noamb.foe :as foe]))

(defprotocol IUserStorage
  "A generic interface for your user storage, be it in RDBMS, NoSQL or another storage engine.
  Defines functions for retrieving and updating a user."
  (update! [this attrs-map] "updates a user record using a map of attributes. Should return an updated user.")
  (find-user [this identifiers] "returns a user record using an identifiers map (for example {:username 'sam'})."))

(defn- salt [] (gensalt 10))

(defn authenticate-from-user
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

(defn start!
  []
  (prn "user module activated")
  (swap! foe/authentication-methods #(into [authenticate-from-user] %)))
