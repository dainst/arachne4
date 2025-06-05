#!/bin/bash

# This script will create an arachne database backup by first exporting
# the whole database excluding the `verwaltung_benutzer` data, then a second
# export with only the table `verwaltung_benutzer`.
# Parameters:
#   $1: Path to target directory

# Read .env file and use as environment variables
export $(cat .env | sed 's/#.*//g' | xargs)


if [ -n "$1" ]
then
	BACKUP_DIR=$1
else
	BACKUP_DIR="/opt/backup"
fi

echo "Backing up to $BACKUP_DIR."

docker exec arachne4_db sh -c "mariadb-dump -uroot -p${DB_ROOT_PASSWORD} --ignore-table-data=arachne.verwaltung_benutzer arachne" > $BACKUP_DIR/arachne_dump.sql
gzip -c $BACKUP_DIR/arachne_dump.sql > $BACKUP_DIR/arachne_dump_$(date +"%Y_%m_%d").gz
rm $BACKUP_DIR/arachne_dump.sql

docker exec arachne4_db sh -c "mariadb-dump -uroot -p${DB_ROOT_PASSWORD} arachne verwaltung_benutzer" > $BACKUP_DIR/verwaltung_benutzer_dump.sql
gzip -c $BACKUP_DIR/verwaltung_benutzer_dump.sql > $BACKUP_DIR/verwaltung_benutzer_dump_$(date +"%Y_%m_%d").gz
rm $BACKUP_DIR/verwaltung_benutzer_dump.sql
