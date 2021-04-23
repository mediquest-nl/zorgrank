#!/bin/bash

USAGE="\nUsage: $0 [name of export file].\n"

if [[ $# -ne 1 ]] ; then
    echo -e $USAGE
    exit 1
fi

FILENAME=$1

echo "Start import..."

psql -U mq_zorgrank_user -h localhost -p 35432 mq_zorgrank < $FILENAME

echo "finished import"
