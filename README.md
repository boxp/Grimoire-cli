# Grimoire

A clojure platform for twitter.

## Depends

- leingen
- java
- twitter4j

## Usage

- Download this repository.

```
git clone <this repository> 
```

- Start grimoire

```
lein run
```

- Print available commands.

```clojure
(help)
```

- Read documents.

```clojure
(doc hoge)
```

- And enjoy your twitter4j hacking.

## Versions

- v0.0.5 2013/5/6
  - statusnum support (HomeTimeline's left numbers, for retweet and fav and more.)
  - retweet command
  - fav command
  - favret command
  - reply command
- v0.0.4 2013/5/6
  - userstream service support (start) (stop)
- v0.0.3 2013/5/5
  - clojure.repl symbols support
  - help commands support
  - standalone support (lein uberjar and runnable on your java!)
- v0.0.2 2013/5/4
  - showtl function and post function.
- v0.0.1 2013/5/3
  - console and post function.

## License

Copyright © 2013 BOXP

Distributed under the Eclipse Public License, the same as Clojure.
