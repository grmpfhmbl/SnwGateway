#!/usr/bin/env bash

TIME="$(/bin/date +%Y-%m-%dT%H:%M:%S%z)"
USER=""
PASSWORD=""

if [ -z ${1+x} ]; then
    echo "File from which to restore not given."
    echo "Usage: restore.sh <filename>"
    exit 1
else
    echo "Will restore $1"
fi

if [ -z ${H2_URL+x} ]; then
    echo "Env variable H2_URL not set. Please set to URL you want to backup from."
    echo "Example: H2_URL=\"jdbc:h2:tcp://localhost:9092/gateway2db/gateway2db\""
    exit 1
else
    echo "Will restore to $H2_URL"
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
    PASSWORD="-password ${H2_PWD}"
fi

read -p "Press any key to continue... CTRL-C to cancel." -n1 -s
echo ""

/usr/bin/java -cp "$BINARY_DIR/h2-$VERSION.jar" org.h2.tools.RunScript -user ${USER} -password $PASSWORD -script "$1" -options compression zip -url "$H2_URL"
