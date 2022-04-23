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

(defn with-style [tag opts]
  (let [tag-classes (-> opts :class (get tag))
        tag-styles (-> opts :style (get tag))
        tag-with-classes (->> (into [(name tag)]
                                  (if (keyword? tag-classes)
                                    [(name tag-classes)]
                                    (map name tag-classes)))
                            (string/join ".")
                            keyword)]
    (if tag-styles
      [tag-with-classes {:style tag-styles}]
      [tag-with-classes])))

(into [:div {:style {:width "40rem"}}] ["some text"])
(apply vector (apply vector [:div]) ["some text"])
(apply vector [:div])

(defn indentation-level [line]
  (if (re-find #"^\s*-" line)
    (-> (string/split line #"- ")
        first
        count) -1))

(defn convert-list-group
  ([list-group] (convert-list-group list-group {}))
  ([list-group opts]
   (let [converted-list (atom '())
         current-list-group (atom (with-style :ul opts))
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
                         (into (with-style :li opts)
                               (if (= 1 (count @current-list-group))
                                 [(string/replace list-item #".*- " "")]
                                 (conj [(string/replace list-item #".*- " "")] @current-list-group))
                                 ))
                  (reset! current-list-group (with-style :ul opts)))
                (swap! current-list-group conj (into (with-style :li opts) [(string/replace list-item #".*- " "")]))))))
     (into (with-style :ul opts) @converted-list))))

(defn convert-group
  "Takes a completed markdown group and returns hiccup-style clojure data structures"
  [group opts]
  ;; (apply vector group)
  (let [group-but-last (into [] (drop-last group))]
    (cond
      ;; regular syntax header
      (-> group-but-last first (string/starts-with? "#"))
      (into (with-style (keyword (str "h" (header-level (-> group-but-last first)))) opts)
            [(-> group-but-last first (string/replace #"#+ " ""))])

      ;; alternative syntax header 1
      (-> group-but-last last (string/starts-with? "="))
      (into (with-style :h1 opts) [(-> group-but-last first)])

      ;; alternative syntax header 2
      (-> group-but-last last (string/starts-with? "--"))
      (into (with-style :h2 opts) [(-> group-but-last first)])

      ;; unordered lists
      (-> group-but-last first (string/starts-with? "- "))
      (convert-list-group group-but-last opts)

      ;; code block
      (-> group-but-last first (string/starts-with? "```"))
      (conj (with-style :pre opts)
            (into (with-style :code opts)
                  [(->> group-but-last
                        rest butlast
                        (map (fn [line] (if (string/blank? line)
                                          "\n"
                                          (str line "\n"))))
                        (string/join "")
                        string/trim-newline)]))

      :else
      (into (with-style :div opts) [(string/join " " group-but-last)]))))

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
                 (let [converted-group (convert-group @current-group opts)]
                   (reset! current-group [])
                   (swap! current-group conj line)
                   (conj coll converted-group))
                 (do
                   (swap! current-group conj line)
                   coll)))
             [:section] (conj (string/split-lines content)
                              " " "DOCUMENT_END")))))
