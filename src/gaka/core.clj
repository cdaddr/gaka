(ns gaka.core
  (:require (clojure.contrib [string :as s])
            (clojure.java [io :as io])))

(def *context* [])
(def *print-indent* true)

(defn make-rule [selector keyvals]
  {:selector selector
   :keyvals keyvals})

(defn indent [n]
  (when *print-indent*
   (s/repeat n "  ")))

(defn render-val [x]
  (cond (number? x) (str x)
        :else (name x)))

(defn render-keyval [n [key val]]
  (when-not val
    (throw (IllegalArgumentException. (str "Missing value for key " (pr-str key) "."))))
  (let [indent (indent n)]
   (str indent (name key) ": " (render-val val) ";")))

(defn- render-keyvals [n sep keyvals]
  (s/join sep (map #(render-keyval n %)
                   (partition-all 2 keyvals))))

(defn render-rule [{:keys [selector keyvals]}]
  (let [indent (indent (dec (count selector)))]
    (if (empty? keyvals)
      ""
      (str indent (s/join " " (map name selector)) " {\n"
           (render-keyvals (count selector) "\n" keyvals)
           "}\n\n"))))

(defn- flatten-seqs
  "Given a vector, return a vector with seqs
  and seqs-of-seqs flattened recursively.
  (seqs in sub-vectors will not be flattened.)"
  [xs]
  (vec
   (loop [xs xs]
     (if (some seq? xs)
       (recur (mapcat #(if (seq? %) % (list %))
                      xs))
       xs))))

(defn- flatten-maps
  "Given a vector, return a vector with one
  level of map flattened."
  [xs]
  (vec
   (mapcat #(if (map? %)
              (flatten (seq %))
              (list %))
           xs)))

(defn- flatten-keyvals [keyvals]
  (flatten-maps (flatten-seqs keyvals)))

(declare compile-rule)
(defn compile* [rules [selector & xs]]
  (reduce (fn [rules selector]
           (binding [*context* (conj *context* selector)]
             (let [xs (flatten-keyvals xs)
                   subrules (filter vector? xs)
                   keyvals (remove vector? xs)
                   rules (conj rules (make-rule *context* keyvals))]
               (reduce (fn [rs x]
                         (compile* rs x))
                       rules subrules))))
          rules (s/split #"\s*,\s*" (name selector))))

(defn css [& rules]
  (let [rules (filter (complement empty?) rules)]
    (if-not (seq rules)
      ""
      (let [rules (reduce compile* [] rules)]
        (s/map-str render-rule rules)))))

(defn inline-css [& keyvals]
  (render-keyvals 0 " " (flatten-keyvals keyvals)))

(defn save-css [filename & rules]
  (with-open [out (io/writer filename)]
    (.write out (apply css rules))))
