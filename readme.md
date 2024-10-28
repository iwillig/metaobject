# Metaobject

This project is an object catalog system for Datalog database
systems. It is designed to help you craft and maintain complex and
object focused database schemas.

Sometimes you want to treat a collection of DataLog attributes as a
single entity or object. For example, let's say we have a person and
email attribute structure. Something like the following,

```clojure
{:email/address {:db/cardinality :db.cardinality/one, :db/type :db.type/string},
 :email/public-id {:db/type :db.type/uuid, :db/unique :db.unique/value},

 :person/emails {:db/cardinality :db.cardinality/many, :db/type :db.type/ref},
 :person/full-name {:db/cardinality :db.cardinality/one, :db/type :db.type/string},
 :person/name {:db/cardinality :db.cardinality/one, :db/type :db.type/string},
 :person/public-id {:db/type :db.type/uuid, :db/unique :db.unique/value}}
```

This works, but we don't have a way of talking about the two entity
types, email and person, in a plain old DataLog database. With
metaobject, you could define those DataLog attributes with the
following.

```clojure
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
        :type :db.type/string}]}]})
```


## Development notes

## Run tests

```shell
bb test
```

## Run Linter

```shell
bb lint
```
