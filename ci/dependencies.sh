#!/usr/bin/env sh

set -eu

# Add python pip and bash
apk add --no-cache --update make py3-pip gcc libc-dev libffi-dev python3-dev openssl-dev

# Install docker-compose via pip
pip3 install -U docker-compose==1.26.0

docker-compose -v
