(ns example.user
  (:refer-clojure :exclude [format])
  (:require [example.macros :refer [regex-spec]]
            [clojure.core.match :refer [match]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [com.gfredericks.test.chuck.generators :as chuck-gen]
            [pancake.core :as pancake]
            [syrup.core.alpha :as syrup]
            [syrup.error.alpha :as syrup-error]
            [syrup.file.alpha :as syrup-file]))

(defmacro defconformer
  [sym f]
  `(def ~sym
     (let [~sym ~f]
       (s/conformer ~sym))))

(defconformer to-int
  (fn [s]
    (if (string? s)
      (try
        (Integer/parseInt s)
        (catch NumberFormatException e ::s/invalid))
      ::s/invalid)))

(defconformer trim
  (fn [s]
    (if (string? s)
      (str/trim s)
      ::s/invalid)))

(defconformer nil-when-blank
  (fn [s]
    (if (string? s)
      (if (str/blank? s)
        nil
        s)
      ::s/invalid)))

(s/def :coerce/int to-int)
(s/def :coerce/trimmed trim)
(s/def :coerce/nil-when-blank nil-when-blank)

(def states #{"WI" "SC" "MN" "NV" "NM" "NE" "AK" "NH" "ME" "NY" "TN" "FL" "IA"
              "GA" "IL" "RI" "VA" "MI" "PA" "UT" "WY" "SD" "MO" "KY" "CT" "AR"
              "ID" "DC" "MA" "OK" "AL" "VT" "MS" "CA" "LA" "DE" "WA" "KS" "MD"
              "ND" "TX" "OR" "NC" "AZ" "IN" "WV" "CO" "HI" "MT" "NJ" "OH"})

(s/def ::name (s/and string? #(<= 4 (count %) 16)))
(s/def ::type #{"super" "regular"})
(s/def ::age (s/int-in 0 126))
(s/def ::gender #{"male" "female"})
(s/def ::state states)
(s/def ::pin (regex-spec #"[0-9]{4}"))
(s/def ::a-code (regex-spec #"[0-9]{5}"))
(s/def ::b-code (regex-spec #"[A-Z0-9]{7}"))
(s/def ::code (s/or :nil nil?
                    :a ::a-code
                    :b ::b-code))

(s/def :domain/user (s/keys :req-un [::name
                                     ::type
                                     ::age
                                     ::gender
                                     ::state
                                     ::pin
                                     ::code]))

(->> :domain/user
     (s/gen)
     (gen/sample)
     (map (comp (partial str/join ",") vals))
     (str/join "\n"))

(def format
  {:id "user"
   :description "User file format."
   :type "delimited"
   :delimiter \,
   :spec :domain/user
   :cells [{:id :name :index 0 :spec trim}
           {:id :type :index 1 :spec trim}
           {:id :age :index 2 :spec to-int}
           {:id :gender :index 3 :spec trim}
           {:id :state :index 4 :spec trim}
           {:id :pin :index 5 :spec trim}
           {:id :code :index 6 :spec nil-when-blank}]})

(defn error-summary
  [error]
  (let [{:keys [pred via in quot-val key quot-key context]} (syrup-error/parts error)]
    (case in
      [:gender] (str quot-val " is not a valid gender; must be male or female.")
      [:state] (str quot-val " is not a state.")
      [:pin] (str quot-val " is not a valid PIN.")
      (match [key pred]
        [:code _] (str "Code " quot-val " is invalid; must be nil, an A code, or a B code.")
        [_ _] (syrup-error/generic-summary error)))))

(defn validate-file [path]
  (syrup-file/validate format error-summary path))

(comment
  (pancake/parse-str format "mike,super,26,male,WI,1234,A1BC5XY")

  (pancake/parse format ["mike,super,26,male,WI,1234,A1BC5XY"
                         "bob,super,43,male,PA,1234,"])

  (syrup-file/validate format error-summary "dev-resources/users.csv")
  )
