(ns noam.util.bench
  (:require [clojure.tools.logging :refer [enabled?]]))

(defmacro bench
  "Returns a vector containing the result of form and the time it took to compute in msec."
  [level form]
  `(if-not (enabled? ~level)
     [~form nil]
     (let [start# (java.lang.System/nanoTime)
           result# ~form
           total-time# (msec-since start#)]
       [result# total-time#])))

(definline msec-since
  "Returns a double representing time in msec since start-time. start-time should be a result of java.lang.System/nanoTime or compatible."
  [start-time]
  `(/
    (double (- (java.lang.System/nanoTime) ~start-time))
    1000000.0))
