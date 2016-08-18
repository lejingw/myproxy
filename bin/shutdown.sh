#!/bin/bash

cd `dirname $0`/..
BASE=`pwd`
PROJECT_PATH=$BASE
LOGFILE="log`date +%Y-%m-%d`.log"

. "$BASE/bin/setenv.sh"

if [ "$ISRUN" == "1" ]; then

    kill -15 `cat $BASE/logs/project.pid`
    echo -n "$PROJECT_DIR_NAME is shutting down"

    if [ -e "$BASE/logs/$LOGFILE" ]; then
        while [ "$ISRUN" == "1" ]; do
            echo -n " ."
            sleep 0.1
            . "$BASE/bin/setenv.sh"
        done
        echo ""
        echo "tail $BASE/logs/$LOGFILE"
        tail $BASE/logs/$LOGFILE
    fi

else

    echo "project is already shutdown!"

fi