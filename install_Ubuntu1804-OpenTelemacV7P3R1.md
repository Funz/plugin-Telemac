# Install OpenTelemac V7P3R1 on Ubuntu 18.04

ref: http://wiki.opentelemac.org/doku.php?id=installation_on_linux

## Dependencies

`sudo apt install hdf5-tools python-scipy python-numpy python2.7 gfortran-4.8 metis-edf mpich`

## get OpenTelemac

`svn co http://svn.opentelemac.org/svn/opentelemac/tags/v7p3r1 ~/opt/telemac-mascaret/v7p3r1`
#username=ot-svn-public
#password=XXXXXXXX

## Configuration

`cd ~/opt/telemac-mascaret/v7p3r1`

`vim telemac.profile`

```
export HOMETEL=$HOME/opt/telemac-mascaret/v7p3r1
export PATH=$HOMETEL/scripts/python27:.:$PATH
export SYSTELCFG=$HOMETEL/configs/systel.cis-ubuntu.cfg
export USETELCFG=ubugfmpich2
export SOURCEFILE=$HOMETEL/configs/pysource.ubugfmpich2.sh
export PYTHONUNBUFFERED='true'
export PYTHONPATH=$HOMETEL/scripts/python27:$PYTHONPATH
export LD_LIBRARY_PATH=$HOMETEL/builds/$USETELCFG/wrap_api/lib:$LD_LIBRARY_PATH
export PYTHONPATH=$HOMETEL/builds/$USETELCFG/wrap_api/lib:$PYTHONPATH
export METISHOME=$HOMETEL/../metis
```

`. telemac.profile`

`vim $SYSTELCFG # Will disable MUMPS dep`
```
> version:    v7p3r1
...
> cmd_obj_c: mpicc -c <srcName> -o <objName>
...
> cmd_obj:    mpif90 -c -g -fbounds-check -Wall -fbacktrace -finit-real=nan -DHAVE_MPI -fconvert=big-endian -frecord-marker=4 <mods> <incs> <f95name>
> #-DHAVE_MUMPS
...
> incs_all:  -I /usr/lib/mpich/include/
> #-I /home/telemac/mumps/MUMPS_5.0.0/include/
> libs_all:  /usr/lib/x86_64-linux-gnu/libmpich.so -lpthread /home/richet/opt/telemac-mascaret/metis/lib/libmetis.so
> #-L /home/telemac/mumps/MUMPS_5.0.0/lib -ldmumps -lmumps_common -lpord /home/telemac/mumps/SCALAPACK/libscalapack.a -L /home/telemac/mumps/BLAS-3.5.0 /home/telemac/mumps/BLAS-3.5.0/blas_LINUX.a /home/telemac/mumps/BLACS/LIB/blacs_MPI-LINUX-0.a /home/telemac/mumps/BLACS/LIB/blacsF77init_MPI-LINUX-0.a /home/telemac/mumps/BLACS/LIB/blacs_MPI-LINUX-0.a -lpthread /home/telemac/metis-5.0.2/libmetis.a
...
```

## Compilation

```
cd $HOMETEL/optionals/metis-5.1.0
cmake -D CMAKE_INSTALL_PREFIX=$HOMETEL/../metis .
make
sudo make install 
```

`compileTELEMAC.py`
