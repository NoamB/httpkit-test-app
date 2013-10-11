(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all] ; get doc in repl
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [httpkit.core :as core]))

(defonce system nil)

(defn create
  "Constructs the current development system."
  []
  (alter-var-root #'system
                  (constantly (core/system))))

(defn start
  "Starts the current development system."
  []
  (alter-var-root #'system core/start))

(defn stop
  "Shuts down and destroys the current development system. Sets system to nil."
  []
  (alter-var-root #'system
                  (fn [s] (when s (core/stop s)))))

(defn create-and-start
  "Initializes the current development system and starts it running."
  []
  (create)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/create-and-start))
