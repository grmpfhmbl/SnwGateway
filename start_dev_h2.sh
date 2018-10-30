#!/usr/bin/env bash

VERSION="1.3.175" ##this is the ancient version of play 2.3.x
#VERSION="1.4.196"
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

/usr/bin/java -Xms32m -Xmx2048m -cp "${BINARY_DIR}/h2-$VERSION.jar" org.h2.tools.Server -tcp -tcpAllowOthers -web -webAllowOthers -baseDir "$DATABASE_DIR"
