#!/usr/bin/env bash

TIME="$(/bin/date +%Y-%m-%dT%H:%M:%S%z)"
USER=""
PWD=""

if [ -z ${H2_URL+x} ]; then
    echo "Env variable H2_URL not set. Please set to URL you want to backup from."
    echo "Example: H2_URL=\"jdbc:h2:tcp://localhost:9092/gateway2db/gateway2db\""
    exit 1
else
    ESCAPE_URL=$(echo $H2_URL | sed "s/^.*h2://g" | sed "s/[^[:alpha:][:digit:].-]/-/g")
    BACKUP_FILE="$BACKUP_DIR/backup-$ESCAPE_URL-$TIME.zip"
    echo "Will backup from $H2_URL to $BACKUP_FILE"
fi

if [ -z ${H2_USER+x} ]; then
    echo "Env variable H2_USER not set. Using empty username."
else
    echo "Username: $H2_USER"
    USER="-user ${H2_USER}"
fi

if [ -z ${H2_PWD+x} ]; then
    echo "Env variable H2_PWD not set. Using empty password."
else
    echo "Password: <set>"
    PWD="-password ${H2_PWD}"
fi

read -p "Press any key to continue... CTRL-C to cancel." -n1 -s
echo ""

/usr/bin/java -cp "$BINARY_DIR/h2-$VERSION.jar" org.h2.tools.Script -user $USER -password $PWD -script "$BACKUP_FILE" -options compression zip -url "$H2_URL"
