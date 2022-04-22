(ns mast.core
  (:require [clojure.string :as string]))

(defn header-level [line]
  (if (string/starts-with? line "#")
    (-> (string/split line #"# ")
        first
        count
        inc) 0))

(defn complete-group? [group]
  (cond
    ;; empty block
    (empty? group)
    false
    ;; blank line
    (and (= 1 (count group))
         (-> group first string/blank?))
    false
    ;; incomplete code block
    (and (or (< (count group) 3)
             (-> group (#(take-last 2 %)) first (string/starts-with? "```") not))
         (-> group first (string/starts-with? "```")))
    false
    ;; no section end
    (not (string/blank? (last group)))
    false
    :else
    true
  ))

(defn convert-group
  "Takes a completed markdown group and returns hiccup-style clojure data structures"
  [group]
  ;; (apply vector group)
  (let [group-but-last (into [] (drop-last group))]
    (cond
      ;; regular syntax header
      (-> group-but-last first (string/starts-with? "#"))
      [(keyword (str "h" (header-level (-> group-but-last first))))
       (-> group-but-last first (string/replace #"#+ " ""))]

      ;; alternative syntax header 1
      (-> group-but-last last (string/starts-with? "="))
      [:h1 (-> group-but-last first)]

      ;; alternative syntax header 2
      (-> group-but-last last (string/starts-with? "--"))
      [:h2 (-> group-but-last first)]

      ;; code block
      (-> group-but-last first (string/starts-with? "```"))
      [:code (->> group-but-last
                 rest butlast
                 (map (fn [line] (if (string/blank? line) "\n" line)))
                 (string/join ""))]

      :else
      (apply vector :div group-but-last))))

(defn md->clj [content]
  (let [current-group (atom [])]
    (reduce (fn [coll line]
              ;; debug
              ;; (println "cg:" (str @current-group))
              ;; (println "co:" (str coll))
              ;; (println "c?:" (complete-group? @current-group))
              ;; (println "li:" line)
              ;; (println "")
              (if (complete-group? @current-group)
                (let [converted-group (convert-group @current-group)]
                  (reset! current-group [])
                  (swap! current-group conj line)
                  (conj coll converted-group))
                (do
                  (swap! current-group conj line)
                  coll)))
            [:div] (conj (string/split-lines content)
                         " " "DOCUMENT_END"))))
