#!/bin/bash
cp gateway2-1.0-SNAPSHOT/conf/application.conf .
rm -rf gateway2-1.0-SNAPSHOT
unzip gateway2-1.0-SNAPSHOT.zip
mv application.conf gateway2-1.0-SNAPSHOT/conf/
