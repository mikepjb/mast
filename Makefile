.PHONY: all test clean

clean:
	rm -rf target/*

repl:
	clojure -M:nrepl:test

test:
	clojure -M:test -m kaocha.runner

build:
	clojure -T:build uber
