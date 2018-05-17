#!/bin/bash

if ! [ $(id -u) = 0 ]; then
   echo "The script need to be run as root." >&2
   exit 1
fi

if [ $SUDO_USER ]; then
    SOCAT_PORT_USER=$SUDO_USER
else
    SOCAT_PORT_USER=$(whoami)
fi

if [ ! $SOCAT_PORT_MASTER ]; then
    SOCAT_PORT_MASTER=master
fi

if [ ! $SOCAT_PORT_SLAVE ]; then
    SOCAT_PORT_SLAVE=slave
fi
echo "Creating '$SOCAT_PORT_MASTER' and '$SOCAT_PORT_SLAVE' for user '$SOCAT_PORT_USER'"
socat -d -d -d -ls pty,link=/dev/$SOCAT_PORT_MASTER,raw,echo=1,user=$SOCAT_PORT_USER,group=staff pty,link=/dev/$SOCAT_PORT_SLAVE,raw,echo=1,user=$SOCAT_PORT_USER,group=staff