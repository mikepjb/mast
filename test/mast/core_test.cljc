(ns mast.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [mast.core :as mast]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Markdown syntax
;; Test Cases taken from:
;; https://www.markdownguide.org/basic-syntax/
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest headings
  (testing "header level counting"
    (is (= 0 (mast/header-level "hello")))
    (is (= 1 (mast/header-level "# hello")))
    (is (= 3 (mast/header-level "### hello"))))

  (testing "header conversion for each level"
    (is (= (mast/md->clj "# Header") [:section [:h1 "Header"]]))
    (is (= (mast/md->clj "## Header") [:section [:h2 "Header"]]))
    (is (= (mast/md->clj "### Alternative") [:section [:h3 "Alternative"]]))))

    (def code-block-example "```
(def clj-var 1)

(+ clj-var 5) => 6
```")

(deftest code-blocks
  (testing "code block conversion (no syntax highlighting)"
    (is (= (mast/md->clj code-block-example) [:section [:code "(def clj-var 1)\n(+ clj-var 5) => 6"]]))
    ))

(deftest lists
  (testing "simple bulleted lists"
    (is (= (mast/md->clj "- one\n- two\n- three")
           [:section [:ul
                      [:li "one"]
                      [:li "two"]
                      [:li "three"]]]))
    )
  
  (testing "indented lists"
    (is (= (mast/md->clj "- one\n- two\n  - two-one\n  - two-two\n- three")
           [:section [:ul
                      [:li "one"]
                      [:li "two"
                       [:ul
                        [:li "two-two"]
                        [:li "two-one"]]]
                      [:li "three"]]]))))

(def combination-mix-1 "# Header

Amazing Content

Alternative Header
==================

Secondary Alternative Header
-----------")

(deftest combinations
  (testing "mix of different header syntaxes and a paragraph"
    (is (= [:section
            [:h1 "Header"]
            [:div "Amazing Content"]
            [:h1 "Alternative Header"]
            [:h2 "Secondary Alternative Header"]]
           (mast/md->clj combination-mix-1)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Unit Testing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; in mast, groups are 'blocks' of complete markdown e.g a complete unordered list
(deftest groups
  (testing "complete group identification"
    (is (not (mast/complete-group? [])))
    (is (not (mast/complete-group? [" "])))
    (is (not (mast/complete-group? ["            "])))
    (is (not (mast/complete-group? ["```"])))
    (is (mast/complete-group? ["```" "some code" "```" " "]))

    (is (not (mast/complete-group? ["Header"])))
    (is (mast/complete-group? ["Header" "=====" " "]))
    (is (mast/complete-group? ["# Header" " "]))
    (is (mast/complete-group? ["Amazing Content" " "]))

    ))


(deftest styling
  (testing "applying style classes to individual tags"
    (is (= (mast/with-style :div {:div [:text-green-400]})
           :div.text-green-400))

    (is (= (mast/with-style :div {:div :italic})
           :div.italic))
    )

  (testing "custom styles"
    (is (= (mast/md->clj "# Header" {:class {:h1 [:text-green-400]}})
           [:section [:h1.text-green-400 "Header"]]))
    (is (= (mast/md->clj "# Header" {:class {:h1 [:text-green-400 :bg-green-900 :shadow]}})
           [:section [:h1.text-green-400.bg-green-900.shadow "Header"]]))))
