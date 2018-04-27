#!/usr/bin/env bash

# exit when any command fails
set -e

export INSTALL_DIR="/opt/snw_gateway"
export PIDFILE="/opt/snw_gateway/pidfile.pid"

SERVICE_UNIT="$INSTALL_DIR/bin/snw_gateway.service"
UPDATE_SCRIPT="$INSTALL_DIR/bin/update_gateway.sh"
export USER="snwgateway"

LIBRXTXPKG=librxtx-java
PKG_OK=$(dpkg-query -W --showformat='${Status}\n' $LIBRXTXPKG|grep "install ok installed")

echo "This script will install SMART Sensornetwork Gateway to $INSTALL_DIR"
read -p "Press any key to continue... CTRL-C to cancel.\n" -n1 -s

if [ ! -f "install_snw_gateway_service.sh" ]; then
    DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

    echo "Your current working directory '$PWD' is not the one where this script is located."
    echo "Changing to $DIR"

    cd "$DIR"
fi

echo "Checking for installed $LIBRXTXPKG..."
if [ "" == "$PKG_OK" ]; then
  echo "Not installed. Installing..."
  sudo apt-get --force-yes --yes install $LIBRXTXPKG
fi

echo "Creating $INSTALL_DIR..."
mkdir -p "$INSTALL_DIR"

echo "Copying files to $INSTALL_DIR..."
cp -R "../../" "$INSTALL_DIR"

echo "Creating updater script..."
envsubst '$INSTALL_DIR $USER $PIDFILE' < update_gateway.sh.template > "$UPDATE_SCRIPT"
chmod +x "$UPDATE_SCRIPT"

echo "Creating service unit $SERVICE_UNIT..."
envsubst '$INSTALL_DIR $USER $PIDFILE' < snw_gateway.service.template > "$SERVICE_UNIT"

echo "Creating $USER and setting directory permissions..."
adduser $USER --system --group --disabled-login --home "$INSTALL_DIR"
adduser $USER dialout
chown -R $USER:$USER "$INSTALL_DIR"

echo "Enabling $SERVICE_UNIT..."
systemctl enable "$SERVICE_UNIT"
