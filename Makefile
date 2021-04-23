.PHONY: all setup-development-db

SHELL = /bin/bash

FILENAME = zorgrank.pgsql

all: setup-development-db

setup-development-db: start-development-db import-to-development-db

start-development-db:
	bin/start_db.sh

import-to-development-db:
	@echo "Start import..."
	bin/import_zorgrank_db.sh $(FILENAME)

clean:
	docker-compose -p zorgrank down
	docker volume prune

psql:
	bin/psql.sh
