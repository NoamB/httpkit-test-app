(ns httpkit.core
  (:require [org.httpkit.server :refer [run-server]])
  (:require [clojure.tools.logging :refer [info error]])
  (:require [ring.middleware.reload :as reload]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route :refer [files not-found]]
            [compojure.handler :refer [site]] ; form, query params decode; cookie; session, etc
            [cheshire.core :as json :refer [generate-string]])
  (:gen-class))

(defn show-landing-page [req]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/generate-string {:foo "barrr" :baz 5})})

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

(defn in-dev?
  [args]
  (some #(= "dev" %) args))

(defn -main
  "Starts the app"
  [& args]
  ;; work around dangerous default behaviour in Clojure
  ;;(alter-var-root #'*read-eval* (constantly false))
  (info "starting up ...")
  (info "args: " args)
  (let [handler (if (in-dev? args)
                  (reload/wrap-reload (site #'all-routes)) ;; only reload when dev
                  (site all-routes))]
    (run-server (-> handler (wrap-logging)) {:port 8080})))
