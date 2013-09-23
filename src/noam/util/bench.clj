(ns noam.util.bench
  (:require [clojure.tools.logging :refer [enabled?]]))

(defmacro bench
  "Returns a vector containing the result of form and the time it took to compute in msec."
  [level form]
  `(let [start# (java.lang.System/nanoTime)
         result# ~form
         total-time# (when (enabled? ~level)
                       (/ (double (- (java.lang.System/nanoTime) start#)) 1000000.0))]
     [result# total-time#]))
