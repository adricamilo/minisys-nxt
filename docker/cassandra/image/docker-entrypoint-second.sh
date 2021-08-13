#!/bin/bash

if [ -z "${REPLICATION_FACTOR}" ]; then
    REPLICATION_FACTOR=1
fi

function create_schema {
    COUNTER=0
    until cat /create-schema.cql | cqlsh; do
	echo "cqlsh: Cassandra is unavailable - retry later"
	(( COUNTER++ ))
	if (( COUNTER >  50 )); then
	    exit -1
	fi
	sleep 2
    done
}

sed -i -e 's/#REPLICATION_FACTOR#/'${REPLICATION_FACTOR}'/g' /create-schema.cql

if [[ $(hostname -s) = ${SCHEMA_SEED_INSTANCE} ]]; then
    create_schema &
fi

exec /usr/local/bin/docker-entrypoint.sh "$@"
