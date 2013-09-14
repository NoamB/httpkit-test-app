(ns httpkit.core
  (:require [org.httpkit.server :refer [run-server]])
  (:require [clojure.tools.logging :refer [info error]])
  (:require [ring.middleware.reload :as reload]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route :refer [resources files not-found]]
            [compojure.handler :refer [site]] ; form, query params decode; cookie; session, etc
            [cheshire.core :as json :refer [generate-string]])
  (:gen-class))

(defn- authenticate
  "Looks in user storage for a user record with the supplied credentials. If found returns true, else false."
  [username password]
  (and (= username "noam")
       (= password "1234"))
  )

(defn show-landing-page [req]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/generate-string {:foo "bar" :baz 5})})

(defn create-login [{params :form-params}]
  (info params)
  (if (authenticate (params "username") (params "password"))
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    (str "you posted login! params: " params) }
    {:status 403
     :headers {}
     :body "wrong credentials!" }))

(defn wrap-logging [handler]
  (fn [req]
    (info "***REQUEST STARTED***" \newline req)
    (let [resp (handler req)]
      (info "Sent Response: " \newline resp)
      (info "---REQUEST ENDED---" \newline)
      resp)))
1
(defroutes all-routes
  (GET "/" [] show-landing-page)
  (POST "/login" [] create-login)
  (route/files "/") ;; static file url prefix /, in `public` folder
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
  (let [dev-mode (in-dev? args) handler (if dev-mode
                                          (reload/wrap-reload (site #'all-routes)) ;; only reload when dev
                                          (site all-routes))]
    (if dev-mode
      (info "--- DEV MODE ---")
      (info "PROD MODE"))
    (run-server (-> handler wrap-logging) {:port 8080})))
