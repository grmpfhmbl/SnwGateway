#!/bin/bash
PID_FILE=./gateway2-1.0-SNAPSHOT/RUNNING_PID
PID=`cat "$PID_FILE"`

if [ -f $PID_FILE ];
then
   echo "Sending SIGINT to $PID"
   kill -SIGINT $PID
else
   echo "File $PID_FILE does not exist."
fi
