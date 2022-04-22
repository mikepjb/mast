(ns mast.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [mast.core :as mast]))

;; Test Cases taken from:
;; https://www.markdownguide.org/basic-syntax/

(deftest headings
  (testing "header level counting"
    (is (= 0 (mast/header-level "hello")))
    (is (= 1 (mast/header-level "# hello")))
    (is (= 3 (mast/header-level "### hello"))))

  (testing "header conversion for each level"
    (is (= (mast/md->clj "# Header") [:div [:h1 "Header"]]))
    (is (= (mast/md->clj "## Header") [:div [:h2 "Header"]]))
    (is (= (mast/md->clj "### Alternative") [:div [:h3 "Alternative"]]))
    )

  )

    (def code-block-example "```
(def clj-var 1)

(+ clj-var 5) => 6
```")

(deftest code-blocks
  (testing "code block conversion (no syntax highlighting)"
    (is (= (mast/md->clj code-block-example) [:div [:code "(def clj-var 1)"]]))
    ))

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

(def combination-mix-1 "# Header

Amazing Content

Alternative Header
==================

Secondary Alternative Header
-----------")

(deftest combinations
  (testing "mix of different header syntaxes and a paragraph"
    (is (= [:div
            [:h1 "Header"]
            [:div "Amazing Content"]
            [:h1 "Alternative Header"]
            [:h2 "Secondary Alternative Header"]]
           (mast/md->clj combination-mix-1)))))
;; TODO combination tests
