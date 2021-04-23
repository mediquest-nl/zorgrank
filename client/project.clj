(defproject nl.mediquest/zorgrank-client "3.0.0"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.844"]
                 [org.clojure/google-closure-library "0.0-20201211-3e6c510d"]
                 [com.google.javascript/closure-compiler-unshaded "v20210302"]
                 [reagent "1.0.0"]
                 [re-frame "1.2.0"]
                 [day8.re-frame/http-fx "0.2.3"]
                 [adzerk/env "0.4.0"]
                 [spec-signature "0.2.0"]
                 [org.slf4j/slf4j-nop "1.7.30"]]
  :middleware [lein-git-down.plugin/inject-properties]
  :plugins [[reifyhealth/lein-git-down "0.3.5"]
            [deraen/lein-sass4clj "0.3.1"]]
  :min-lein-version "2.5.3"
  :source-paths ["src/cljs"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :sass {:target-path "resources/public/"
         :source-paths ["resources/public/sass"]}
  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.3"]
                   [re-frisk "1.4.0"]
                   [deraen/sass4clj "0.5.4"]
                   [thheller/shadow-cljs "2.12.5"]]}
   :prod {}})
