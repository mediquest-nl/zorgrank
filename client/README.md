# nl.mediquest/zorgrank-client

## Prerequisites

- [Leiningen](https://github.com/technomancy/leiningen) to build the ClojureScript project
- [NPM](https://www.npmjs.com/) to get Javascript dependencies
- [Shadow Cljs](https://shadow-cljs.github.io/docs/UsersGuide.html) to develop / build ClojureScript

## Development Mode

### Install dependencies
```
lein clean
npm install
lein sass4clj once
```

### Run application:

```
lein sass4clj once
npx shadow-cljs watch dev
```

Use

```
lein sass4clj auto
```

to watch for file changes.

Navigate to [localhost:3449](http://localhost:3449).

### Run from Emacs

```
M-x cider-jack-in-clojurescript
Select ClojureScript REPL type: shadow
Select shadow-cljs build (e.g. dev): dev
```

## Production Build

To compile clojurescript to javascript:

```
lein clean
npm install
lein sass4clj once
npx shadow-cljs release prod
```

## Documentation
See the [docs](docs/).

- [External API](docs/external-api.md)
