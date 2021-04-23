#!/usr/bin/env bash

docker exec -ti --user postgres zorgrank_db psql -U mq_zorgrank_user mq_zorgrank
