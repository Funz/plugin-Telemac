#!/bin/bash

. $HOME/opt/telemac-mascaret/v7p3r1/telemac.profile

telemac2d.py -c ubugfmpich2 $* &
PID=$!
echo $PID >> PID
wait $PID
rm -f PID
