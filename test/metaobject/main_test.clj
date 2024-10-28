(ns metaobject.main-test
  (:require [clojure.test :as t :refer [deftest testing is]]
            [metaobject.main :as metaobj]))

(def subject
  (metaobj/obj-catalog
   {::metaobj/types
    [(metaobj/obj-type
      {::metaobj/name ::person
       ::metaobj/attrs
       [(metaobj/obj-attr {::metaobj/name ::name
                           ::metaobj/type :db.type/string
                           ::metaobj/obj-name ::person})

        (metaobj/obj-attr {::metaobj/name ::full-name
                           ::metaobj/type :db.type/string
                           ::metaobj/obj-name ::person})

        (metaobj/obj-ref {::metaobj/name ::emails
                          ::metaobj/ref-obj-name ::email
                          ::metaobj/cardinality :db.cardinality/many
                          ::metaobj/obj-name ::person})]})

     (metaobj/obj-type
      {::metaobj/name ::email
       ::metaobj/attrs
       [(metaobj/obj-attr {::metaobj/name ::address
                           ::metaobj/type :db.type/string
                           ::metaobj/obj-name ::email})]})]}))

(deftest check-okay-test

  (testing "Context of the test assertions"
    #_(is (true? false))

    #_(is (= {}
           (metaobj/obj-attr {::metaobj/name ::name})))

    #_(is (= {}
           (metaobj/obj-type {::metaobj/name ::person})))

    (is (= {}
           (metaobj/to-schema-tx subject)))

    (is (= {}
           (metaobj/to-create-inputs subject)))

    #_(is (= {}
           (metaobj/obj-ref {::metaobj/obj-name ::person
                             ::metaobj/name ::email})))


    #_(is (= [] {}))))
