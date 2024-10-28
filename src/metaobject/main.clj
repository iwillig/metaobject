(ns metaobject.main)

(def one-cardinality :db.cardinality/one)
(def many-cardinality :db.cardinality/many)

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

(defn- safe-name
  [key-str]
  (cond
    (string? key-str)
    key-str
    (keyword? key-str)
    (name key-str)
    :else nil))

(defn- key-from-object-name
  [obj-name key-value]
  (keyword (safe-name obj-name)
           (safe-name key-value)))

(defrecord ObjCatalog [types]
  Obj-Inputs
  (to-create-inputs [self]
    (into (sorted-map)
          (map to-create-inputs types)))
  Obj-Schemable
  (to-schema-tx [_self]
    (into (sorted-map)
          (map (comp second to-schema-tx) types))))

(defn- attr-inputs
  [{:keys [identy type]}]
  [(keyword (safe-name identy))
   (keyword (safe-name type))])

(defn- attr-db-schema
  [{:keys [_identy type cardinality unique]}]
  (merge
   {:db/type type}
   (when cardinality
     {:db/cardinality cardinality})
   (when unique
     {:db/unique unique})))

(defrecord ObjType [name attrs]

  Obj-Inputs
  (to-create-inputs [_self]
    [name
     (into [:map {:closed true}]
           (map to-create-inputs attrs))])

  Obj-Schemable
  (to-schema-tx [_self]
    [name
     (into {(key-from-object-name name "public-id")
            public-id-schema}
           (map to-schema-tx attrs))]))

(defrecord ObjAttr [identy
                    name
                    obj-name
                    type
                    cardinality]
  Obj-Inputs
  (to-create-inputs [self]
    (attr-inputs self))

  Obj-Schemable
  (to-schema-tx [self]
    [identy (attr-db-schema self)]))

(defrecord ObjRef [identy
                   type
                   obj-name
                   cardinality]
  Obj-Inputs
  (to-create-inputs [self]
    (attr-inputs self))
  Obj-Schemable
  (to-schema-tx [self]
    [identy
     (attr-db-schema self)]))

(defn obj-catalog
  [{::keys [types]}]
  (map->ObjCatalog {:types types}))

(defn obj-type
  [{::keys [name attrs]}]
  (map->ObjType
   {:name  name
    :attrs attrs}))

(defn obj-attr
  [{::keys [identy
            obj-name
            name
            type
            cardinality]}]
  (let [new-obj-attr-identy (or (key-from-object-name obj-name name)
                                identy)]
    (map->ObjAttr {:identy      new-obj-attr-identy
                   :name        name
                   :obj-name    obj-name
                   :type        type
                   :cardinality (or cardinality one-cardinality)})))

(defn obj-ref
  [{::keys [identy
            obj-name
            ref-obj-name
            name
            cardinality]}]
  (let [new-ref-identy (or (key-from-object-name obj-name name)
                           identy)]

    (map->ObjRef {:identy       new-ref-identy
                  :type         db-type-ref
                  :ref-obj-name ref-obj-name
                  :obj-name     obj-name
                  :cardinality  (or cardinality
                                   one-cardinality)})))
