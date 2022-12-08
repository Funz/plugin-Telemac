#!/bin/bash

CAS=$1

if [ 1 -le `echo \`pwd -P\` | grep --color='auto' -P -n "[\x80-\xFF]" | wc -l` ]; then
  echo "Telemac will not support non ISO char in path. Exiting."; 
  exit 1
fi

NCPU=`grep ^cpu\\\\scores /proc/cpuinfo | uniq |  awk '{print $4}'`

## singularity run using docker image:
singularity run -B `echo $PWD`:/workdir -H /workdir docker://irsn/telemac-mascaret:latest telemac2d.py --ncsize=$NCPU $CAS &
## singularity run using singularity image (faster within SLURM): 1st, move docker image to singularity with `singularity build --sandbox telemac-mascaret docker://irsn/telemac-mascaret:latest`
#module load singularity
#singularity run -B `echo $PWD`:/workdir -H /workdir telemac-mascaret telemac2d.py --ncsize=$NCPU $CAS &

PID=$!
echo $PID >> PID
wait $PID
rm -f PID
