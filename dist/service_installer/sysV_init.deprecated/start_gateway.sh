#!/usr/bin/env bash
# this file should be copied to bin. it's a quick and dirty hack and not for production use!

#this whole stuff will be managed via application.ini in play 2.4. See: https://www.playframework.com/documentation/2.4.x/Production

#./gateway2-1.0-SNAPSHOT/bin/gateway2 -DapplyEvolutions.default=true -mem 128 -Djava.library.path=/usr/lib/jni -Dconfig.file=gateway2-1.0-SNAPSHOT/conf/application.conf -Dgnu.io.rxtx.SerialPorts=/dev/ttyAMA0
./gateway2-1.0-SNAPSHOT/bin/gateway2 -DapplyEvolutions.default=true -mem 128 -Djava.library.path=/usr/lib/jni -Dconfig.file=gateway2-1.0-SNAPSHOT/conf/application.conf
