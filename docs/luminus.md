# Luminus

Mediquest Zorgrank API is based on the [Luminus web framework][1] with the following additions:

* +reitit adds [Reitit][2] Clojure/Script router support
* +jetty adds Jetty webserver support to the project
* +swagger adds support for Swagger-UI
* +service create a service application without the front-end boilerplate such as HTML templates
* +postgres adds db.core namespace and add PostreSQL dependencies

To set up the framework:

    lein new luminus test-logging +reitit +service +jetty +swagger +postgres

## Environment variables

Luminus has a [fine grained model][3] for configuration.

To overwrite the version controlled `dev` en `test` config, you can put the following edn's in the root of your project:

### dev-config.edn

``` clojure
;; WARNING
;; The dev-config.edn file is used for local environment variables, such as database credentials.
;; This file is listed in .gitignore and will be excluded from version control by Git.

{:dev true
 :port 3000
 ;; when :nrepl-port is set the application starts the nREPL server on load
 :nrepl-port 7000
 }
```

### test-config.edn

``` clojure
;; WARNING
;; The test-config.edn file is used for local environment variables, such as database credentials.
;; This file is listed in .gitignore and will be excluded from version control by Git.

{:port 3000
 }
```

[1]: http://www.luminusweb.net/docs/profiles.html
[2]: https://github.com/metosin/reitit/
[3]: http://www.luminusweb.net/docs/environment.html
