(ns noam.user)

(defprotocol IUserStorage
  (update-attributes [this attrs-map])
  (find-by-identifiers [this identifiers]))

(defrecord User [id username encrypted-password])

;; go to storage
(defn- getUser [username encrypted-password]
  (if (and (= username "noam")
           (= encrypted-password "1234"))
    (->User 1 username encrypted-password)
    nil))

(defn authenticate
  "Looks in user storage for a user record with the supplied credentials. If found returns the User, else nil."
  [credentials]
  (getUser (first credentials) (second credentials)))
