#!/bin/bash
docker-compose -p zorgrank down
docker-compose -p zorgrank up -d

docker-compose up -d

echo "Waiting 10 seconds till PostgreSQL is ready..."
sleep 10s

docker exec -i --user postgres zorgrank_db createdb mq_zorgrank

docker exec -i --user postgres zorgrank_db psql mq_zorgrank -a  <<__END
CREATE USER mq_zorgrank_user PASSWORD 'mq_zorgrank_password';
__END

docker exec -i --user postgres zorgrank_db psql mq_zorgrank < zorgrank.pgsql
