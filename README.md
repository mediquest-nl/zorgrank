# ZorgRank

This repository contains the code of [ZorgRank][1] which is part of the project 'Gepast Verwijzen'. 
The project aims to help a general practitioner and patient to make joint decisions about the most suitable follow-up care by ranking care providers with an
arithmetic selection model we call ZorgRank. See [https://gepastverwijzen.nl/][2] for more information about the project. 

This repository contains:

- a sample set of data
- the [API][3] by which the data can be accessed
- an [example client][4] that uses the API to show how ZorgRank works

[1]: https://zorgrank-demo.mediquest.cloud
[2]: https://gepastverwijzen.nl
[3]: https://zorgrank-demo.mediquest.cloud/api/api-docs/index.html
[4]: https://zorgrank-client-demo.mediquest.cloud

## Project structure

- [bin](bin) contains scripts to start the application and database (for example used in the Makefile)
- [client](client) an example website that uses the API to show how ZorgRank works
- [docs](docs) specific documentation of subsections
- [env](env) environment specific settings for development and production
- [resources](resources) resources like database migration scripts, queries on the database, configuration files and images
- [src/nl/mediquest/zorgrank](src/nl/mediquest/zorgrank) the code of the API structured using the [Luminus](https://luminusweb.com/) web framework
- [test/nl/mediquest/zorgrank](test/nl/mediquest/zorgrank) unit, integration and load tests
- [zorgrank.pgsql](zorgrank.pgsql) SQL-script to import data into PostgreSQL

## Development

To start the application you need to setup a local database and insert data and then start the API. You will need Leiningen 2.0 or above installed and Docker Compose for running a local PostgreSQL instance. 

### Prerequisites
The project is written in [Clojure][3] and uses [Leiningen][4] as a build tool. To install Clojure and Leiningen see [Purely Functional's installation instructions](https://purelyfunctional.tv/guide/how-to-install-clojure/).

[Docker Compose][5] is necessary for running a local PostgreSQL instance.

[3]: https//clojure.org
[4]: https://github.com/technomancy/leiningen
[5]: https://docs.docker.com/compose/install/

### Setup database

To setup a database server for the application, run:

    make
    # enter password: mq_zorgrank_password

* This script imports the tables from `zorgrank.pgsql` in the root of the project. This is smaller example of the actual data available on the Mediquest network.

To connect with the local database run `make psql`.

### Start API

#### Emacs

Running from Emacs start a REPL and evaluate `(start)`:

    M-x cider-jack-in-clj
    user> (start)

#### Leiningen

Running from Leiningen, in the root of the project run:

    lein run

#### REPL

To start from a Leiningen REPL use:

    lein repl
    user> (start)

---

When started navigate to [localhost:3030][6] to see the Swagger API of the started application.

[6]: http://localhost:3030

To investigate the API with the [client](client) run `cd client && npm install && lein sass4clj once && npx shadow-cljs watch dev`. The client interface will run on [localhost:3449](localhost:3449) and uses the started API.

### Clean up

To clean up Docker artifacts, run:

    make clean

## Testing

#### Unit tests

Run unit tests:

    lein test :unit

### Integration tests

Run integration tests (the ZorgRank API is started in integration test):

    make setup-development-db
    lein test :integration

### Integration and unit tests

    make setup-development-db
    lein test

### Load tests

For load testing see the [load](load) folder or the [Load Testing docs](docs/testing/load.md).

## Available data

The data in this open sourced API is a subset of the data available in the production API as used by general practitioners. To see what data is limited see the [description of available data](docs/data.md).

## Client application

See the [client](client) subfolder for an application that uses the ZorgRank API. This application is deployed at [https://zorgrank-client-demo.mediquest.cloud](https://zorgrank-client-demo.mediquest.cloud) and there uses the deployed version of this API.

## Deployment

Instances of this demo API and client are deployed in the Mediquest cluster at [zorgrank-demo.mediquest.cloud](https://zorgrank-demo.mediquest.cloud) and [zorgrank-client-demo.mediquest.cloud](https://zorgrank-client-demo.mediquest.cloud).

## Documentation

See the [docs](docs/) folder.

- [Overview](docs/overview.md)
- [Luminus](docs/luminus.md)
- [Description of available data](docs/data.md)
- Database:
    - [Local development](docs/database/development.md)
    - [Querying from the application using HugSQL](docs/database/hugsql.md)
    - [Migrations](docs/database/migrations.md)
- [Authentication](docs/authentication.md)
- [Monitoring](docs/monitoring.md)
- Testing:
    - [Load testing](docs/testing/load.md)
    - [Integration testing](docs/testing/integration.md)
- [clojure.spec's instrumentation](docs/instrumentation.md)

## Disclaimer

See [DISCLAIMER.md](DISCLAIMER.md).

## License

[MIT](LICENSE) © Mediquest
