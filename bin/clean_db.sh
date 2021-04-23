#!/bin/bash
docker exec mq_zorgrank_postgres dropdb mq_zorgrank -U mq_zorgrank_user
if [ $? -eq 0 ]
then
  echo "Dropped the mq_zorgrank database from the Postgres Docker container"
fi
