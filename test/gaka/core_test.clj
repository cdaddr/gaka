(ns gaka.core-test
  (:use [gaka.core] :reload-all)
  (:use [clojure.test]))

(defmacro =? [& body]
  `(are [x# y#] (= x# y#)
        ~@body))

(deftest test-compile*
  (=? (compile* []  [:a])
      [{:selector ["a"]
        :keyvals []}]

      (compile* []  [:a :color :red])
      [{:selector ["a"]
        :keyvals [:color :red]}]

      (compile* [] [:a [:img :border :none]])
      [{:selector ["a"]
        :keyvals []}
       {:selector ["a" "img"]
        :keyvals [:border :none]}]

      (compile* [] [:div [:a [:img :border :none]]])
      [{:selector ["div"]
        :keyvals []}
       {:selector ["div" "a"]
        :keyvals []}
       {:selector ["div" "a" "img"]
        :keyvals [:border :none]}]

      (compile* [] [:div (list :border :none)])
      [{:selector ["div"]
        :keyvals [:border :none]}]

      (compile* [] [:div ["a, img" :border :none]])
      [{:selector ["div"]
        :keyvals []}
       {:selector ["div" "a"]
        :keyvals [:border :none]}
       {:selector ["div" "img"]
        :keyvals [:border :none]}]))

(deftest test-render-rule
  (=? (render-rule {:selector ["a"] :keyvals [:color :red]})
      "a {\n  color: red;}\n\n"

      (render-rule {:selector ["a"] :keyvals [:color :red :border :none]})
      "a {\n  color: red;\n  border: none;}\n\n"

      (render-rule {:selector ["a" "img"] :keyvals [:border :none]})
      "  a img {\n    border: none;}\n\n"))

(deftest test-css
  (=? (css nil)
      ""
      
      (css [:a :color :red [:img :border :none]])
      "a {\n  color: red;}\n\n  a img {\n    border: none;}\n\n"

      (css [:a :color :red [:img :border :none] :font-style :italic])
      "a {\n  color: red;\n  font-style: italic;}\n\n  a img {\n    border: none;}\n\n"))
