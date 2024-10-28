(ns metaobject.builders
  (:require [cuerdas.core :as str]))

(defn safe-name
  [key-str]
  (cond
    (string? key-str)
    key-str
    (keyword? key-str)
    (name key-str)
    :else nil))

(defn combined-keys
  [obj-name key-value]
  (keyword (safe-name obj-name)
           (safe-name key-value)))

(defn build-identy
  [obj-name attr-name identy]
  (let [new-identy (or (combined-keys obj-name attr-name)
                       identy)]
    {:identy new-identy
     :snake-identy (str/snake (safe-name new-identy))}))

(defn attr-inputs
  [{:keys [identy type]}]
  [(keyword (safe-name identy))
   (keyword (safe-name type))])

(defn attr-db-schema
  [{:keys [_identy type cardinality unique]}]
  (merge
   {:db/type type}
   (when cardinality
     {:db/cardinality cardinality})
   (when unique
     {:db/unique unique})))
