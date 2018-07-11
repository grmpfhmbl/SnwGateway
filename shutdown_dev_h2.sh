#!/usr/bin/env bash

VERSION="1.3.175" ##this is the ancient version of play 2.3.x
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BINARY_DIR="${SCRIPT_DIR}/target/h2"

/usr/bin/java -cp "${BINARY_DIR}/h2-$VERSION.jar" org.h2.tools.Server -tcpShutdown tcp://localhost:9092
