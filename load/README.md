# zorgrank-load-test

Load test for the ZorgRank API using [Gatling](https://www.gatling.io).

## Usage

Ensure the `CLIENT_KEY` environment variable is set if used by the application under test.

Run the project directly:

    $ clojure -m nl.mediquest.zorgrank-load-test.core https://localhost:3030

Location of test report will be shown in output.

## Java 11

The current version of http-kit used in Gatling doesn't support Java 11 (https://github.com/http-kit/http-kit/pull/383). See [this instruction](https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-on-ubuntu-18-04) on how to host multiple versions of Java on your machine.

## License

Copyright © Mediquest
