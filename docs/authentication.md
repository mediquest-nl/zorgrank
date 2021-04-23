# Authentication

We authenticate clients based on a `client-key` in the header
of a request. The client-key is queried against a `clients` hash-map
with a uuid as keyword and the name of the organization as value.

The way in which we expose the clients depends on the development
stage. On production, the clients are sourced by an edn file with the
following structure:

    {:<guid> "mediquest"
     :<guid2> "zorgrank-client"}

The development environment doesn't use an edn file but returns a
hash-map immediately.

See '/env/[prod|dev]/clj/nl/mediquest/zorgrank/clients.clj' for
the source code.

It's possible to allow anonymous clients to use the api, to do this
set `:authentication-active?` to false in
'/env/[prod|dev]/resources/config.edn'.
