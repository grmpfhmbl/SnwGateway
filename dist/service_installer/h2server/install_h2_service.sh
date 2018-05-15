#!/usr/bin/env bash

# exit when any command fails
set -e


## latest version (official downoad URL is http://www.h2database.com/html/download.html)
#export VERSION=1.4.195
export VERSION="1.3.175" ##this is the ancient version of play 2.3.x
DOWNLOAD_URL="http://central.maven.org/maven2/com/h2database/h2/$VERSION/h2-$VERSION.jar"

export INSTALL_DIR="/opt/h2"
export BINARY_DIR="$INSTALL_DIR/bin"
export LOG_DIR="$INSTALL_DIR/log"
export DATABASE_DIR="$INSTALL_DIR/db"
export BACKUP_DIR="$INSTALL_DIR/backups"
SERVICE_UNIT="$BINARY_DIR/h2server.service"

USER="h2"

echo "This script will install H2 ($VERSION) to $INSTALL_DIR"
echo "Databases will be stored in $DATABASE_DIR"
echo "JAR will be downloaded from $DOWNLOAD_URL"

read -p "Press any key to continue... CTRL-C to cancel." -n1 -s


mkdir -p "$INSTALL_DIR"
mkdir -p "$BINARY_DIR"
mkdir -p "$DATABASE_DIR"
mkdir -p "$LOG_DIR"
mkdir -p "$BACKUP_DIR"

wget "$DOWNLOAD_URL" -nc -nd -P "$BINARY_DIR"

envsubst '$VERSION $INSTALL_DIR $BINARY_DIR $LOG_DIR $DATABASE_DIR $USER' < h2server.service.template > "$SERVICE_UNIT"
envsubst '$VERSION $BINARY_DIR $BACKUP_DIR' < backup_h2.sh > "$BINARY_DIR/backup_h2.sh"
envsubst '$VERSION $BINARY_DIR' < restore_h2.sh > "$BINARY_DIR/restore_h2.sh"
envsubst '$VERSION $BINARY_DIR' < runscript_h2.sh > "$BINARY_DIR/runscript_h2.sh"

adduser ${USER} --system --group --disabled-login --home "$INSTALL_DIR"
chown -R ${USER}:${USER} "$INSTALL_DIR"

systemctl enable "$SERVICE_UNIT"
systemctl status "$SERVICE_UNIT"

chmod +x "$INSTALL_DIR/bin/*.sh"