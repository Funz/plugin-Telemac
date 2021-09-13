#!/bin/bash

CAS=$1

if [ 1 -le `echo \`pwd -P\` | grep --color='auto' -P -n "[\x80-\xFF]" | wc -l` ]; then
  echo "Telemac will not support non ISO char in path. Exiting."; 
  exit 1
fi

. $HOME/opt/telemac-mascaret/v7p3r1/telemac.profile

telemac2d.py -c ubugfmpich2 $CAS &
PID=$!
echo $PID >> PID
wait $PID
rm -f PID
