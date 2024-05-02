#!/bin/bash

# This script will import a arachne database dump into to local mariadb (orchestrated with docker).
# Parameters:
#   $1: Path to zipped Arachne database dump

# Read .env file and use as environment variables
export $(cat .env | sed 's/#.*//g' | xargs)

gunzip < $1 | docker exec -i arachne4_db mariadb -uroot -p${DB_ROOT_PASSWORD}
