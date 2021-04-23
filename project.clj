(defproject nl.mediquest.zorgrank "3.0.0"

  :description "Zorgrank API"
  :url "https://zorgrank-demo.mediquest.cloud"
  :license "MIT"

  :dependencies [[cheshire "5.10.0"] ;; Clojure JSON and JSON SMILE (binary json format) encoding/decoding
                 [clojure.java-time "0.3.2"]
                 [cprop "0.1.17"]
                 [funcool/struct "1.4.0"]
                 [luminus-jetty "0.2.1"]
                 [luminus-transit "0.1.2"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.5"]
                 [metosin/muuntaja "0.6.7"]
                 [metosin/reitit "0.5.6"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.8.2"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.3.610"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/test.check "1.1.0"]

                 ;; Logging
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/slf4j-api "1.7.30"]
                 [org.slf4j/jul-to-slf4j "1.7.30"]
                 [org.slf4j/jcl-over-slf4j "1.7.30"]
                 [org.slf4j/log4j-over-slf4j "1.7.30"]
                 [org.clojure/tools.logging "1.1.0"]
                 [ring-logger "1.0.1"]
                 [ring.middleware.conditional "0.2.0"]

                 ;; Sentry
                 [io.sentry/sentry-logback "1.7.30"]

                 ;; Database
                 [org.postgresql/postgresql "42.2.5"] ;; Java JDBC 4.2 (JRE 8+) driver for PostgreSQL database
                 [conman "0.8.3"] ;; Luminus database connection management and SQL query generation library
                 [com.layerware/hugsql "0.4.9"] ;; Hugsql is implicitly used by conman, but it's also needed for debugging sql queries
                 [luminus-migrations "0.7.1"] ;; Command line wrapper for Migratus (a general migration framework)
                 [luminus-immutant "0.2.5"] ;; Immutant HTTP adapter for Luminus

                 [org.webjars.npm/bulma "0.9.1"]
                 [org.webjars.npm/material-icons "0.3.1"]
                 [org.webjars/webjars-locator "0.40"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.8.2"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-cors "0.1.13"]
                 [selmer "1.12.31"]
                 [camel-snake-kebab "0.4.1"]
                 [expound "0.8.6"]

                 ;; Http
                 [http-kit "2.5.0"]]

  :min-lein-version "2.0.0"
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot nl.mediquest.zorgrank.core

  :plugins []

  :test-selectors {:unit (complement :integration)
                   :integration :integration}
  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "zorgrank.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[org.clojure/tools.namespace "1.0.0"]
                                 [expound "0.8.6"]
                                 [pjstadig/humane-test-output "0.10.0"]
                                 [prone "2020-01-17"]
                                 [ring/ring-devel "1.8.2"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]]
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
