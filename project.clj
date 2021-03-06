(defproject httpkit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-json "0.2.0"]
                 [http-kit "2.1.13"]
                 [compojure "1.1.5"]
                 ; logs
                 [org.slf4j/slf4j-api "1.7.5"]
                 [org.slf4j/slf4j-log4j12 "1.7.5"]
                 [org.clojure/tools.logging "0.2.6"]
                 ; bcrypt
                 [clj-bcrypt-wrapper "0.1.0"]
                 ; db
                 [c3p0/c3p0 "0.9.1.2"]
                 [org.clojure/java.jdbc "0.3.0-alpha5"]
                 [mysql/mysql-connector-java "5.1.22"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [org.clojure/java.classpath "0.2.0"]
                                  [stylefruits/gniazdo "0.0.0"]]
                   :plugins [;[lein-ring "0.8.7"]
                              [lein-kibit "0.0.8"]
                              [lein-exec "0.3.1"]]}}
  :main httpkit.core)
