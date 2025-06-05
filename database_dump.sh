#!/bin/bash

# This script will create an arachne database dump.

# Read .env file and use as environment variables
export $(cat .env | sed 's/#.*//g' | xargs)

BACKUP_DIR="/opt/backup"

docker exec arachne4_db sh -c "mariadb-dump -uroot -p${DB_ROOT_PASSWORD} arachne" > $BACKUP_DIR/arachne_dump_complete.sql
gzip -c $BACKUP_DIR/arachne_dump_complete.sql > $BACKUP_DIR/arachne_dump_complete_$(date +"%Y_%m_%d").gz
rm $BACKUP_DIR/arachne_dump_complete.sql
