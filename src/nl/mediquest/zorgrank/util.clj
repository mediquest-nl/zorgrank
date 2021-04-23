(ns nl.mediquest.zorgrank.util
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as string])
  (:import
   (java.util Collections Comparator)))

(defn round
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

(defn remove-path-params
  [uri path-params]
  (reduce #(string/replace %1 (str \/ %2) "")
          uri
          (vals path-params)))

(s/fdef update-in-when
  :args (s/cat :m map? :path (s/coll-of any?) :f fn?)
  :ret map?)
(defn update-in-when
  [m path f]
  (if (reduce get m path)
    (update-in m path f)
    m))

(defn file->edn [resource-path]
  (-> resource-path
      io/resource
      slurp
      edn/read-string))

(def lower-is-better (fnil compare Integer/MAX_VALUE  Integer/MAX_VALUE))

(def higher-is-better (fnil #(compare %2 %1) 0 0))

(defn xf-sort
  "See: https://github.com/cgrand/xforms"
  ([] (sort compare))
  ([cmp]
   (fn [rf]
     (let [buf (java.util.ArrayList.)]
       (fn
         ([] (rf))
         ([acc] (rf (reduce rf acc (doto buf (Collections/sort cmp)))))
         ([acc x] (.add buf x) acc))))))

(defn xf-sort-by
  "See: https://github.com/cgrand/xforms"
  ([kfn] (sort-by kfn compare))
  ([kfn cmp]
   (xf-sort (fn [a b]
              (.compare ^Comparator cmp (kfn a) (kfn b))))))

(defn sql-str
  [[query & params]]
  (let [query (reduce (fn [q p]
                        (let [replacement (cond (string? p) (format "'%s'" p)
                                                (nil? p) "null"
                                                :else p)]
                          (string/replace-first q \? replacement)))
                      query
                      params)]
    (str "!!!!!!!!!!! BEGIN QUERY !!!!!!!!!!!\n"
         query
         \;
         "!!!!!!!!! END QUERY !!!!!!!!!\n")))
