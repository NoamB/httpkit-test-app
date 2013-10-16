(ns noam.util.db
  (:require [clojure.tools.logging :refer [info]]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc.ddl :as ddl]
            [clojure.string :as s]
            [noam.util.bench :refer [bench]]
            [noamb.foe.user :refer [IUserStorage] :as user])
  (:import javax.sql.DataSource
           com.mchange.v2.c3p0.ComboPooledDataSource
           noamb.foe.user.User))

(def db-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/test"
   :user "annonymous"
   :password ""})

(defn connection-pool
  "Create a connection pool for the given database spec."
  [{:keys [subprotocol subname classname user password
           excess-timeout idle-timeout minimum-pool-size maximum-pool-size
           test-connection-query
           idle-connection-test-period
           test-connection-on-checkin
           test-connection-on-checkout]
    :or {excess-timeout (* 30 60)
         idle-timeout (* 3 60 60)
         minimum-pool-size 3
         maximum-pool-size 15
         test-connection-query nil
         idle-connection-test-period 0
         test-connection-on-checkin false
         test-connection-on-checkout false}
    :as spec}]
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass classname)
                 (.setJdbcUrl (str "jdbc:" subprotocol ":" subname))
                 (.setUser user)
                 (.setPassword password)
                 (.setMaxIdleTimeExcessConnections excess-timeout)
                 (.setMaxIdleTime idle-timeout)
                 (.setMinPoolSize minimum-pool-size)
                 (.setMaxPoolSize maximum-pool-size)
                 (.setIdleConnectionTestPeriod idle-connection-test-period)
                 (.setTestConnectionOnCheckin test-connection-on-checkin)
                 (.setTestConnectionOnCheckout test-connection-on-checkout)
                 (.setPreferredTestQuery test-connection-query))})

(def pooled-db (delay (connection-pool db-spec)))

(defn db-connection [] @pooled-db)

(defn create-users-sql
  "Retruns SQL to create a Users table, currently without sql engine and charset and keys and foreign keys."
  []
  (ddl/create-table
    :Users
    [:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
    [:username "varchar(255)"]
    [:encrypted-password "varchar(255)"]))

(defn exec
  [sql-vec]
  (info sql-vec)
  (jdbc/execute! (db-connection) sql-vec))

(defn select
  [sql-vec]
  (let [result (bench :info (jdbc/query (db-connection) sql-vec))
        resp (first result)
        request-time (second result)]
    (info "[<<--]" sql-vec
          (when request-time
            (str "(" request-time " msec)")))
    resp))

(defn map-to-prepared-statement
  "Serializes a map of attributes into an SQL-compatible query part."
  [m]
  (let [ks (map #(str (name %) " = ?") (keys m))
        vs (vals m)]
    [(s/join "," ks) vs]))

;; TODO: kill this monstrosity
(defn build-query-from-attrs
  "Builds a query starting with query-prefix, a string generated from serializing attrs-map and query-suffix.
   Returns a vector with the query as prepared statement and a vector of parameter values."
  [query-prefix attrs-map query-suffix]
  (let [id (:id attrs-map)
        m (dissoc attrs-map :id)
        ps (map-to-prepared-statement m)
        query (str query-prefix (first ps) query-suffix)]
    (flatten
      [query (second ps) id])))

(deftype MySQLUserStorage
         []
  IUserStorage
  (update!
      [this attrs-map]
    (exec (build-query-from-attrs "UPDATE Users SET " attrs-map " WHERE id = ?")))
  (find-user
      ^user/User
      [this identifiers]
    (let [ps (map-to-prepared-statement identifiers)
          query (str "SELECT * FROM Users WHERE " (first ps))
          params (second ps)
          query-and-params (cons query params)]
      (user/map->User (first (select query-and-params))))))
