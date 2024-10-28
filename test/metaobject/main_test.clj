(ns metaobject.main-test
  (:require [clojure.test :as t :refer [deftest testing is]]
            [datascript.core :as d]
            [metaobject.main :as metaobj]))

(def subject
  (metaobj/obj-catalog
   {:obj-types
    [{:name ::person
      :attrs
      [{:name ::name
        :type :db.type/string}
       {:name ::full-name
        :type :db.type/string}
       {:name         ::emails
        :ref-obj-name ::email
        :cardinality  :db.cardinality/many}]}
     {:name ::email
      :attrs
      [{:name ::address
        :type :db.type/string}]}]}))

(deftest check-okay-test
  (testing "Context of the test assertions"
    (let [subject-tx (metaobj/to-schema-tx subject)]

      (is (= {} subject))

      (is (= {}
             subject-tx))

      (is (= {}
             (d/create-conn subject-tx)))

      (is (= {}
             (metaobj/to-create-inputs subject))))))
