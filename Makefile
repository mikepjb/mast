.PHONY: build

clean:
	rm -rf target/*

repl:
	clojure -M:nrepl:test

test:
	clojure -M:test -m kaocha.runner

build-jar:
	if [ -f mast.jar ]; then rm mast.jar; fi && clj -M:pack mach.pack.alpha.skinny --no-libs --project-path mast.jar

deploy:
	CLOJARS_USERNAME="mikepjb" CLOJARS_PASSWORD=$$(pass show clojars-deploy) clojure -A:deploy

build: test build-jar deploy
