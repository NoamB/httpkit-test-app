(ns noam.util.db-test
  (:use
    [clojure.test]
    [noam.util.db]))

(deftest test-map-to-prepared-statement
  (testing "testing attrs-str"
    (is (= (map-to-prepared-statement {:id 1 :name "moshe"})
           ["name = ?,id = ?" '("moshe" 1)]))))
