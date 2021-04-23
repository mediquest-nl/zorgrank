# Querying

HugSQL is used to generate functions from queries. See [Luminus embraces HugSQL][1].

For debugging purposes it's possible to turn on a sql printer when in the repl:

    (require '[nl.mediquest.zorgrank.config :refer [*print-sql*]]
    (alter-var-root #'*print-sql* (constantly true))

[1]: https://yogthos.net/posts/2016-02-22-LuminusEmbracingHugSQL.html
