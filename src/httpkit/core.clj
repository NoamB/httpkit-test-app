(ns httpkit.core
  (:use org.httpkit.server)
  (:use [clojure.tools.logging :only (info error)])
  (:use compojure.core)
  (:require [compojure.route :as route])
  (:require [cheshire.core :refer :all])
  (:gen-class))

(defn show-landing-page [req]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (generate-string {:foo "bar" :baz 5})})

(defn wrap-logging [handler]
  (fn [req]
    (info "***REQUEST STARTED***" \newline req)
    (let [resp (handler req)]
      (info "Sent Response: " \newline resp)
      (info "---REQUEST ENDED---" \newline)
      resp)))

(defroutes all-routes
  (GET "/" [] show-landing-page)
  (route/files "/static/") ;; static file url prefix /static, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

(defn -main
  "Starts the app"
  [& args]
  ;; work around dangerous default behaviour in Clojure
  ;;(alter-var-root #'*read-eval* (constantly false))
  (info "starting up ...")
  (run-server (-> all-routes (wrap-logging)) {:port 8080})
)
