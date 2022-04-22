(ns mast.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [mast.core :as mast]))

;; Test Cases taken from:
;; https://www.markdownguide.org/basic-syntax/

(deftest headings

  (testing "simple h1 header"
    (is (= (mast/md->clj "# Header") [:div [:h1 "Header"]]))
    )

  )

;; TODO combination tests
