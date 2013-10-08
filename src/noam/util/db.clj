(ns noam.util.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc.ddl :as ddl]
            [clojure.string :as s])
  (:import javax.sql.DataSource
           com.mchange.v2.c3p0.ComboPooledDataSource))

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
   [:encrypt_password "varchar(255)"]))

(defn get-user-by-id
  [id]
  (jdbc/query (db-connection)
              ["SELECT * FROM Users WHERE id = ?" id]))

(defn exec
  [sql-params]
  (jdbc/execute! (db-connection) sql-params))

(defn select
  [sql-params]
  (jdbc/query (db-connection) sql-params))

(defn attrs-str
  "Serializes a map of attributes into an SQL-compatible query part."
  [m]
  (let [ks (map #(str (name %) " = ?") (keys m))
        vs (vals m)]
    [(s/join "," ks) vs]))

(defn build-query-from-attrs
  "Builds a query starting with query-prefix, a string generated from serializing attrs-map and query-suffix.
   Returns a vector with the query as prepared statement and a vector of parameter values."
  [query-prefix attrs-map query-suffix]
  (let [id (:id attrs-map)
        m (dissoc attrs-map :id)
        as (attrs-str m)
        query (str query-prefix (first as) query-suffix)]
  (flatten
   [query (second as) id])))
