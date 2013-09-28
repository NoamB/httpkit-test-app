(ns noam.util.db
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]
            [clojure.java.jdbc.ddl :as ddl])
  (:import javax.sql.DataSource
           com.mchange.v2.c3p0.ComboPooledDataSource))

(def db-spec
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//localhost:3306/test"
   :user "annonymous"
   :password ""})

(defn pool
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(def pooled-db (delay (pool db-spec)))

(defn db-connection [] @pooled-db)

(defn create-blogs-sql
  "Create a table to store blog entries"
  []
  (ddl/create-table
   :blogs
   [:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
   [:title "varchar(255)"]
   [:body :text]))

(defn get-blog-entry
  [id]
  (jdbc/query (db-connection)
              ["SELECT title, body FROM blogs WHERE id = ?" id]))

(defn exec
  [sql]
  (jdbc/execute! (db-connection) [sql]))

(defn select
  [sql]
  (jdbc/query (db-connection) [sql]))
