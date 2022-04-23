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

(defn with-style [tag style-map]
  (let [tag-style (get style-map tag)]
    (->> (into [(name tag)]
               (if (keyword? tag-style)
                 [(name tag-style)]
                 (map name tag-style)))
         (string/join ".")
         keyword)))

(defn indentation-level [line]
  (if (re-find #"^\s*-" line)
    (-> (string/split line #"- ")
        first
        count) -1))

(defn convert-list-group
  [list-group]
  (let [converted-list (atom '())
        current-list-group (atom [:ul])
        current-indentation (atom 0)]
    (->> list-group
         reverse
         (#(doseq [list-item %]
             ;; if indentation is 0 we know it's a root list item and cannot be a child
             ;; if indentation is not 0 it could be a child
             ;; support for 3 or n levels of indentation?
             (if (zero? (indentation-level list-item))
               (do
                 (swap! converted-list conj
                        (into [:li (string/replace list-item #".*- " "")]
                              (when-not (= [:ul] @current-list-group)
                                [@current-list-group])))
                 (reset! current-list-group [:ul]))
               (do
                 (swap! current-list-group conj [:li (string/replace list-item #".*- " "")]))))))
    (into [:ul] @converted-list)))

(defn convert-group
  "Takes a completed markdown group and returns hiccup-style clojure data structures"
  [group class-map]
  ;; (apply vector group)
  (let [group-but-last (into [] (drop-last group))]
    (cond
      ;; regular syntax header
      (-> group-but-last first (string/starts-with? "#"))
      [(with-style (keyword (str "h" (header-level (-> group-but-last first)))) class-map)
       (-> group-but-last first (string/replace #"#+ " ""))]

      ;; alternative syntax header 1
      (-> group-but-last last (string/starts-with? "="))
      [(with-style :h1 class-map) (-> group-but-last first)]

      ;; alternative syntax header 2
      (-> group-but-last last (string/starts-with? "--"))
      [(with-style :h2 class-map) (-> group-but-last first)]

      ;; unordered lists
      (-> group-but-last first (string/starts-with? "- "))
      (convert-list-group group-but-last)

      ;; code block
      (-> group-but-last first (string/starts-with? "```"))
      [(with-style :code class-map) (->> group-but-last
                 rest butlast
                 (map (fn [line] (if (string/blank? line) "\n" line)))
                 (string/join ""))]

      :else
      (apply vector (with-style :div class-map) group-but-last))))

(defn md->clj
  ([content]
   (md->clj content {}))
  ([content opts]
   (let [current-group (atom [])]
     (reduce (fn [coll line]
               ;; debug
               ;; (println "cg:" (str @current-group))
               ;; (println "co:" (str coll))
               ;; (println "c?:" (complete-group? @current-group))
               ;; (println "li:" line)
               ;; (println "")
               (if (complete-group? @current-group)
                 (let [converted-group (convert-group @current-group (:class opts))]
                   (reset! current-group [])
                   (swap! current-group conj line)
                   (conj coll converted-group))
                 (do
                   (swap! current-group conj line)
                   coll)))
             [:section] (conj (string/split-lines content)
                              " " "DOCUMENT_END")))))
