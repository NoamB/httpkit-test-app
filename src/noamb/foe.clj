(ns noamb.foe)

(def authentication-methods (atom []))

(defn- as-foe-symbol
  [kword]
  (symbol (str "noamb.foe." (name kword))))

(defn start!
  [config]
  (doseq [module (:modules config)]
         (let [module-ns (as-foe-symbol module)]
           (require module-ns)
           (load-string (str "(" (name module-ns) "/start!)")))))

(defn stop!
  []
  (reset! authentication-methods []))

