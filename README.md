[![.github/workflows/ant.yml](https://github.com/Funz/plugin-Telemac/actions/workflows/ant.yml/badge.svg)](https://github.com/Funz/plugin-Telemac/actions/workflows/ant.yml)

# Funz plugin: Telemac

This plugin is dedicated to launch R calculations from Funz.
It supports the following syntax and features:

  * Input
    * file type supported: *.cas, any other format for resources (.slf, ...)
    * __need__ a *.poi file which setup output Points Of Interest coordinates:
      ```
      first_poi=2500,15
      second_poi=2900,250
      sampledline10points=2500,15:10:2900,250
      map10x10points=2500,15:10,10:2900,250
      ...
      ```
    * parameter syntax: 
      * variable syntax: `$(...)`
      * formula syntax: `@{...}`
      * comment char: `/`
    * example input files:
      * .cas file
        ```
        RESULTS FILE              ='r2d_breach.slf'
        BOUNDARY CONDITIONS FILE ='geo_breach.cli'
        LIQUID BOUNDARIES FILE    ='t2d_breach.liq'
        GEOMETRY FILE               ='geo_breach.slf'
        PREVIOUS COMPUTATION FILE        ='ini_breach.slf'
        BREACHES DATA FILE     ='breach.txt'
        ...
        VARIABLES FOR GRAPHIC PRINTOUTS =U,V,S,H,B,L,D,W
        ...
        VELOCITY DIFFUSIVITY =$(vel~0.005)
        ...
        INITIAL ELEVATION        =$(init_elev~[1.5,2.5])
        ...
        ```
      * .poi file (dedicated to specify Funz output Points Of Interest):
        ```
        xylowercenter=2500,15
        xymediumcenter=2500,250
        xylowerright=2900,15
        xymediumright=2900,250
        allmap=2500,15:10,10:2900,250
        .notreadmap=2500,15:100,100:2900,250
        ```
      * will identify input:
        * vel, expected to default value 0.005
        * init_elev, expected to vary inside [1.5,2.5]
  * Output
    * file type supported: *.slf `RESULTS FILE` (Seraphin format)
    * read any `VARIABLES FOR GRAPHIC PRINTOUTS` at .poi points, along time
    * will return time dependant arrays like: `H_xylowerright`=`[2.3383100032806396,2.3411474227905273,2.3438708782196045,2.3465609550476074,...]`
    * will return time dependant maps (10x10 pixels) like: `H_allmap`=`[[2.3383100032806396,2.3411474227905273,...],[...],...]]`
    * will **NOT** return ignored maps (100x100 pixels, too much) like: `H_.notreadmap`=`[[2.3383100032806396,2.3411474227905273,...],[...],...]]`



![Analytics](https://ga-beacon.appspot.com/UA-109580-20/plugin-Telemac)
