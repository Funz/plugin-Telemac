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
export HOMETEL=\$HOME/opt/telemac-mascaret/v7p3r1
export PATH=\$HOMETEL/scripts/python27:.:\$PATH
export SYSTELCFG=\$HOMETEL/configs/systel.cis-ubuntu.cfg
export USETELCFG=ubugfmpich2
export SOURCEFILE=\$HOMETEL/configs/pysource.ubugfmpich2.sh
export PYTHONUNBUFFERED='true'
export PYTHONPATH=\$HOMETEL/scripts/python27:\$PYTHONPATH
export LD_LIBRARY_PATH=\$HOMETEL/builds/\$USETELCFG/wrap_api/lib:\$LD_LIBRARY_PATH
export PYTHONPATH=\$HOMETEL/builds/\$USETELCFG/wrap_api/lib:\$PYTHONPATH
export METISHOME=\$HOMETEL/../metis
export LD_LIBRARY_PATH=\$METISHOME/lib:\$LD_LIBRARY_PATH
```

`. telemac.profile`

`vim $SYSTELCFG # Will disable MUMPS dep`
```
[Configurations]
configs:    ubugfmpich2
[general]
version:    v7p3r1
modules:    system
cmd_lib:    ar cru <libname> <objs>
mods_all:   -I <config>
sfx_zip:    .gztar
sfx_lib:    .a
sfx_obj:    .o
sfx_mod:    .mod
sfx_exe:
val_root:   <root>/examples
val_rank:   all
cmd_obj_c: mpicc -c <srcName> -o <objName>
[ubugfmpich2]
brief: parallel mode, using mpiexec directly (of the MPICH2 package).
mpi_cmdexec:   mpiexec -wdir <wdir> -n <ncsize> <exename>
cmd_obj:    mpif90 -cpp -c -g -fbounds-check -Wall -fbacktrace -finit-real=nan -DHAVE_MPI -fconvert=big-endian -frecord-marker=4 <mods> <incs> <f95name>
cmd_exe:    mpif90 -cpp -fconvert=big-endian -frecord-marker=4 -v -lm -o <exename> <objs>  <libs>
incs_all:  -I /usr/lib/mpich/include/
libs_all:  /usr/lib/x86_64-linux-gnu/libmpich.so -lpthread $HOME/opt/telemac-mascaret/metis/lib/libmetis.*
```

## Compilation

```
cd $HOMETEL/optionals/metis-5.1.0
cmake -D CMAKE_INSTALL_PREFIX=$HOMETEL/../metis .
make
sudo make install 
```

`python2.7 $HOMETEL/scripts/python27/compileTELEMAC.py --clean`
