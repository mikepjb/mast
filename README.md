# Mast

**M**arkdown **A**nd **St**yles

Mast is a library for converting markdown text into hiccup-style clojure:

```markdown
# Title

- List
- Of
- Things
```

```clojure
[:div
 [:h1 "Title"]
 [:ul
  [:li "List"]
  [:li "Of"]
  [:li "Things"]]]
```

It's main use case is for markdown conversion but is also written to support styling the generated
html elements/tags. It is made this way to work with functional approaches for applying CSS such as
TailwindCSS.

Mast works in both Clojure and Clojurescript.

## Getting Started

```clojure
(require 'core.hypalynx.mast :as mast)

(mast/md->clj "# Markdown Content")

(mast/md->clj "# Markdown Content" {:styles {:li [:list-disc]}})
```