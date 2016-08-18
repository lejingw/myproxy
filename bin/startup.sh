#!/bin/bash

cd `dirname $0`/..
BASE=`pwd`
PROJECT_PATH=$BASE
LOGFILE="log`date +%Y-%m-%d`.log"

. "$BASE/bin/setenv.sh"

if [ "$ISRUN" != "1" ]; then

    if [ ! -d "$BASE/logs" ]; then
        mkdir -p $BASE/logs
    fi
    if [ ! -e "$BASE/logs/$LOGFILE" ]; then
        echo "try to create log file ..."
        touch $BASE/logs/$LOGFILE
    else
        echo "try to append log ..."
	    echo ">>>>===================================================================<<<<" >> $BASE/logs/$LOGFILE
	    echo ">>>>                                                                   <<<<" >> $BASE/logs/$LOGFILE
	    echo ">>>>                           the_new_start                           <<<<" >> $BASE/logs/$LOGFILE
	    echo ">>>>                                                                   <<<<" >> $BASE/logs/$LOGFILE
	    echo ">>>>===================================================================<<<<" >> $BASE/logs/$LOGFILE
    fi

    echo "classpath:"
    echo "$CLASSPATH"
    echo "starting project ..."

    nohup $JAVA_HOME/bin/java $JAVA_CONF -classpath $CLASSPATH com.lejingw.apps.proxy.MysqlProxyServer >> $BASE/logs/$LOGFILE 2>&1 &
    echo $! > $BASE/logs/project.pid
    tail -f $BASE/logs/$LOGFILE

    echo "project started successfully"

else

    echo "project is already running."

fi
