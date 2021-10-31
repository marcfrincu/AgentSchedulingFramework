#!/bin/sh

# This script is responsible for starting an agent module
# It is usually called by the Healing module
# Arguments:

# $1 generated folder
# $2 module type
# $3 true (paused) | false (ready_to_run)
# $4 parent UUID
# $5 IP|URI of the remote host
# $6 true (started from remote host) | false (local start)
# $7  true|false (external module or part of platform)
# $8 pathToArchive (HDFS or local)
# $9 scriptToStartModule

#THE_CLASSPATH=

for i in `ls *.jar`
do
	THE_CLASSPATH=${THE_CLASSPATH}:${i}
done;

#for i in `ls drools-5.1.0/*.jar`
#do
#	THE_CLASSPATH=${THE_CLASSPATH}:${i}
#done;

#echo $THE_CLASSPATH

java -cp ".$THE_CLASSPATH" runner/RunModule $2 $3 $4 $5 $6 $7 $8 $9
