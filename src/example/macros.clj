(ns example.macros
  (:require [clojure.spec.alpha :as s]
            [com.gfredericks.test.chuck.generators :as chuck-gen]))

(defmacro regex-spec [re]
  `(s/spec (s/and string? #(re-matches ~re %))
           :gen #(chuck-gen/string-from-regex ~re)))

(defmacro defconformer
  [sym docstring f]
  `(def ~sym
     ~docstring
     (let [~sym ~f]
       (s/conformer ~sym))))
