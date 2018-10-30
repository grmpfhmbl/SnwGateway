#!/usr/bin/env bash

VERSION="1.3.175" ##this is the ancient version of play 2.3.x
##VERSION="1.4.196"
DOWNLOAD_URL="http://central.maven.org/maven2/com/h2database/h2/$VERSION/h2-$VERSION.jar"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BINARY_DIR="${SCRIPT_DIR}/target/h2"
DATABASE_DIR="${SCRIPT_DIR}/target/db"

mkdir -p ${BINARY_DIR}
mkdir -p ${DATABASE_DIR}
cd ${BINARY_DIR}

if [ ! -f "${BINARY_DIR}/h2-$VERSION.jar" ]; then
    echo "Could not find '${BINARY_DIR}/h2-$VERSION.jar'. Downloading H2 jar..."
    curl "${DOWNLOAD_URL}" -o "${BINARY_DIR}/h2-$VERSION.jar"
fi

/usr/bin/java -Xms32m -Xmx512m -cp "${BINARY_DIR}/h2-$VERSION.jar" org.h2.tools.Script -url "jdbc:h2:tcp://localhost:9092/gateway2db/gateway2db" -user gateway -password p9hnm3420 -script "${SCRIPT_DIR}/backup.zip" -options compression zip

echo "Shutdown DB server, delete old database, restart DB server.".
read -p "Press any key to re-import database... CTRL-C to cancel." -n1 -s

##/usr/bin/java -Xms32m -Xmx512m -cp "${BINARY_DIR}/h2-$VERSION.jar" org.h2.tools.RunScript -url "jdbc:h2:tcp://localhost:9092/gateway2db/gateway2db;DB_CLOSE_DELAY=-1" -user gateway -password p9hnm3420 -script backup.zip -options compression zip
##/usr/bin/java -Xms32m -Xmx512m -cp "${BINARY_DIR}/h2-$VERSION.jar" org.h2.tools.RunScript -url "jdbc:h2:tcp://localhost:9092/gateway2db/gateway2db;DB_CLOSE_DELAY=-1" -continueOnError -user gateway -password p9hnm3420 -script /Users/steffen/temp/SensDB_2017-10-18.dump.measurements