# Mast

**M**arkdown **A**nd **St**yles

Mast is a library for converting markdown text into hiccup-style clojure data structures:

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

Include mast as a dependency in your project: 
```clojure
[com.hypalynx/mast "0.1.5"]
{com.hypalynx/mast {:mvn/version "0.1.5"}}
```

```clojure
(require 'core.hypalynx.mast :as mast)

(mast/md->clj "# Markdown Content")

(mast/md->clj "# Markdown Content" {:styles {:li [:list-disc]}})
```

## Examples

```clojure
[:div (mast/md->clj markdown-text
                    {:style
                     {:div  {:width "40rem"}}
                     :class
                     {:h1   :text-2xl.mb-10
                      :h2   :text-xl.mb-6
                      :h3   :text-lg.mb-4
                      :ul   :pl-8.my-8
                      :li   :list-disc
                      :code :bg-slate-200.inset-shadow.rounded.p-2.text-sm}})]
```
