(ns example.main
  (:require [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.tools.cli :as cli]
            [example.user :as user]
            [syrup.core.alpha :as syrup]
            [syrup.error.alpha :as syrup-error]
            [syrup.file.alpha :as syrup-file]
            [taoensso.timbre :as log])
  (:gen-class :main true))

(defn exists? [arg] (.exists (io/file arg)))

(def exit-errors
  {:ok-or-valid {:exit-code 0 :summary "If validation was run, all records in specified file are valid. Otherwise, command completed successfully."}
   :invalid {:exit-code 1 :summary "Some or all records in specified file are invalid."}
   :exception {:exit-code 2 :summary "An exception is thrown."}
   :bad-args {:exit-code 3 :summary "Arguments are invalid."}
   :file-not-found {:exit-code 4 :summary "The specified file is not found."}})

(defn exit-code [k] (:exit-code (get exit-errors k)))

(def cli-options
  [["-v" "--verbose" "Make the operation more talkative"]
   ["-h" "--help" "Shows usage information"]])

(defn usage [options-summary]
  (->> ["Usage: data-validation [options] file-path"
        ""
        "Options:"
        options-summary
        ""]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn error-summary
  [error]
  (let [{:keys [pred via in quot-val key quot-key context]} (syrup-error/parts error)]
    (match [key pred]
      [:code _] (str "Code " quot-val " is invalid; must be nil, an A code, or a B code.")
      [_ _] (syrup-error/generic-summary error))))

(defn fail [code message]
  (binding [*out* *err*]
    (println message)
    code))

(def exits
  (str "Exit codes:\n"
       (->> exit-errors
           (vals)
           (map (fn [{:keys [exit-code summary]}]
                  (str "  " exit-code "  " summary)))
           (str/join "\n"))))

(defn bad-args [message]
  (fail (exit-code :bad-args) message))

(defn file-not-found [path]
  (fail (exit-code :file-not-found) (str "fatal: file '" path "' not found." )))


(defn main
  [args]
  (try
    (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
          path (first arguments)]
      (log/set-level! (if (:verbose options) :debug :info))
      (cond (:help options) (do (println (usage summary))
                                (println exits))
            errors (bad-args (error-msg errors))
            (not path) (bad-args (usage summary))
            (not (exists? path)) (file-not-found path)
            :else (exit-code (if (user/validate-file path) :valid :invalid))))
    (catch Exception ex
      (log/error ex (str "Script threw exception."))
      (exit-code :exception))))

(defn -main [& args] (System/exit (or (main args) 0)))
