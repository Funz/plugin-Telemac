#!/bin/bash

CAS=$1

if [ 1 -le `echo \`pwd -P\` | grep --color='auto' -P -n "[\x80-\xFF]" | wc -l` ]; then
  echo "Telemac will not support non ISO char in path. Exiting."; 
  exit 1
fi

NCPU=`grep ^cpu\\\\scores /proc/cpuinfo | uniq |  awk '{print $4}'`

docker run `echo $PWD`:/tmp nicogodet/telemac:v8p2r1 bash -c "source ../setenv.sh && cd /tmp && telemac2d.py --ncsize=$NCPU $CAS" &

PID=$!
echo $PID >> PID
wait $PID
rm -f PID
