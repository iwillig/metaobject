(ns metaobject.main
  (:require [metaobject.builders :as builders]
            [cuerdas.core :as str]))

(def one-cardinality :db.cardinality/one)
;;(def _many-cardinality :db.cardinality/many)

(def db-type-ref :db.type/ref)

(defprotocol Obj-Schemable
  "A protocol to transform an metaobject object into a datalog schemas."
  (to-schema-tx [self]
    "A function that takes an metaobject object and transform it into
    a datalog schema."))

(defprotocol Obj-Inputs
  "A protocol to define transformations into mali maps that can be used
  for CRUD operations."
  (to-create-inputs [self]))

(def public-id-schema
  {:db/type   :db.type/uuid
   :db/unique :db.unique/value})

(defrecord ObjCatalog [obj-types]
  Obj-Inputs
  (to-create-inputs [_self]
    (into (sorted-map)
          (map to-create-inputs obj-types)))
  Obj-Schemable
  (to-schema-tx [_self]
    (into (sorted-map)
          (map (comp second to-schema-tx) obj-types))))


(defrecord ObjType [name attrs snake-identy]

  Obj-Inputs
  (to-create-inputs [_self]
    [name
     (into [:map {:closed true}]
           (map to-create-inputs attrs))])

  Obj-Schemable
  (to-schema-tx [_self]
    [name
     (into {(builders/combined-keys name "public-id")
            public-id-schema}
           (map to-schema-tx attrs))]))

(defrecord ObjAttr [identy
                    snake-identy
                    name
                    obj-name
                    type
                    cardinality]
  Obj-Inputs
  (to-create-inputs [self]
    (builders/attr-inputs self))

  Obj-Schemable
  (to-schema-tx [self]
    [identy (builders/attr-db-schema self)]))

(defrecord ObjRef [identy
                   snake-identy
                   type
                   obj-name
                   cardinality]
  Obj-Inputs
  (to-create-inputs [_self]
    #_(builders/attr-inputs self)
    {})
  Obj-Schemable
  (to-schema-tx [self]
    [identy
     (builders/attr-db-schema self)]))

(defn obj-ref
  [{:keys [identy
           obj-name
           ref-obj-name
           name
           cardinality] :as _opts}]
  (map->ObjRef
   (merge
    (builders/build-identy obj-name name identy)
    {:type         db-type-ref
     :ref-obj-name ref-obj-name
     :obj-name     obj-name
     :cardinality  (or cardinality
                       one-cardinality)})))

(defn obj-attr-like?
  [x]
  (or (instance? ObjRef x)
      (instance? ObjAttr x)))

(defn obj-like?
  [x]
  (instance? ObjType x))

(defn obj-attr
  [{:keys [identy
           obj-name
           name
           type
           cardinality] :as opts}]
  (if (:ref-obj-name opts)
    (obj-ref opts)
    (map->ObjAttr
     (merge
      (builders/build-identy obj-name name identy)
      {:name        name
       :obj-name    obj-name
       :type        type
       :cardinality (or cardinality one-cardinality)}))))

(defn obj-type
  [{:keys [name attrs]}]
  (map->ObjType
   {:name  name
    :attrs (mapv
            (fn [x]
              (if-not (obj-attr-like? x)
                (obj-attr (assoc x :obj-name name))
                x))
            attrs)
    :snake-identy (str/snake name)}))


(defn obj-catalog
  [{:keys [obj-types]}]
  (map->ObjCatalog
   {:obj-types
    (mapv (fn [x]
            (if-not (obj-like? x)
              (obj-type x)
              x))
          obj-types)}))
