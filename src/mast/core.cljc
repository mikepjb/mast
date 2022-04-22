(ns mast.core
  (:require [clojure.string :as string]))

(defn md->clj [content]
  [:div
   (if (string/starts-with? content "# ")
     [:h1 (string/replace content #"#* " "")]
     content)])
