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

- Put your consumerKey and consumerSecret in keys.clj. 

```clojure
{:consumerKey "erzzup" :consumerSecret "akameco"}
```

```
lein run
```

- Post Tweet

``` clojure
(post "hoge")
```

- Show HomeTimeline

```clojure
(showtl)
```

- And enjoy your twitter4j hacking.

## Versions

- v0.0.2 showtl function and post function.
- v0.0.1 console and post function.

## License

Copyright © 2013 BOXP

Distributed under the Eclipse Public License, the same as Clojure.
