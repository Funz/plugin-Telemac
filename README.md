[![.github/workflows/ant.yml](https://github.com/Funz/plugin-Telemac/actions/workflows/ant.yml/badge.svg)](https://github.com/Funz/plugin-Telemac/actions/workflows/ant.yml)

# Funz plugin: Telemac

This plugin is dedicated to launch R calculations from Funz.
It supports the following syntax and features:

  * Input
    * file type supported: *.cas, any other format for resources (.slf, ...)
    * __need__ a *.poi file which setup output Points Of Interest coordinates:
      ```
      first_poi=2500,15
      second_poi=2500,250
      ...
      ```
    * parameter syntax: 
      * variable syntax: `$(...)`
      * formula syntax: `@{...}`
      * comment char: `/`
    * example input files:
      * .cas file
        ```
        /---------------------------------------------------------------------
        / TELEMAC2D Version v6p1 16 juil. 2012
        / TELEMAC 2D : rupture digue  - Qinitial
        /---------------------------------------------------------------------
        /
        / ENTREES-SORTIES, FICHIERS
        /---------------------------------------------------------------------
        /
        RESULTS FILE              ='r2d_breach.slf'
        /
        BOUNDARY CONDITIONS FILE ='geo_breach.cli'
        /
        LIQUID BOUNDARIES FILE    ='t2d_breach.liq'
        /
        GEOMETRY FILE               ='geo_breach.slf'
        /
        PREVIOUS COMPUTATION FILE        ='ini_breach.slf'
        /
        BREACHES DATA FILE     ='breach.txt'
        /
        /---------------------------------------------------------------------
        / ENTREES-SORTIES, GENERALITES
        /---------------------------------------------------------------------
        /
        COMPUTATION CONTINUED =true
        /
        /-------------------------------------------------------------------
        /---
        /
        TITLE='TELEMAC 2D : rupture digue  - Qinitial'
        /
        /
        /---------------------------------------------------------------------
        / ENTREES-SORTIES, GRAPHIQUES ET LISTING
        /---------------------------------------------------------------------
        /
        MASS-BALANCE                        =YES
        /
        LISTING PRINTOUT PERIOD             =20
        /
        VARIABLES FOR GRAPHIC PRINTOUTS =U,V,S,H,B,L,D,W
        /
        GRAPHIC PRINTOUT PERIOD   =120
        /
        INFORMATION ABOUT SOLVER           =YES
        /
        /
        /---------------------------------------------------------------------
        / EQUATIONS
        /---------------------------------------------------------------------
        /
        FRICTION COEFFICIENT             =15.
        /
        VELOCITY DIFFUSIVITY =$(vel~0.005)
        /
        LAW OF BOTTOM FRICTION         =3
        /
        /
        /---------------------------------------------------------------------
        / EQUATIONS, CONDITIONS INITIALES
        /---------------------------------------------------------------------
        /
        INITIAL CONDITIONS ='COTE CONSTANTE'
        /
        INITIAL ELEVATION        =$(init_elev~[1.5,2.5])
        /
        /
        /---------------------------------------------------------------------
        / EQUATIONS, CONDITIONS LIMITES
        /---------------------------------------------------------------------
        /
        PRESCRIBED FLOWRATES                      =0;50.0
        /
        VELOCITY PROFILES                  =1;4
        /
        PRESCRIBED ELEVATIONS                      =2.0;0
        /
        OPTION FOR LIQUID BOUNDARIES =1;1
        /
        /
        /---------------------------------------------------------------------
        / PARAMETRES NUMERIQUES
        /---------------------------------------------------------------------
        /
        BREACH                                     =YES
        /
        TREATMENT OF THE LINEAR SYSTEM             =2
        /
        FREE SURFACE GRADIENT COMPATIBILITY =0.9
        /
        INITIAL TIME SET TO ZERO                     =true
        /
        TIME STEP                               =0.5
        /
        SUPG OPTION                             =1;2
        /
        DURATION                            =2700
        /
        CONTINUITY CORRECTION                   =true
        /
        /---------------------------------------------------------------------
        / PARAMETRES NUMERIQUES, SOLVEUR
        /---------------------------------------------------------------------
        /
        SOLVER              =1
        /
        SOLVER ACCURACY =1.E-5
        /
        /
        /---------------------------------------------------------------------
        / PARAMETRES NUMERIQUES, VITESSE-CELERITE-HAUTEUR
        /---------------------------------------------------------------------
        /
        MASS-LUMPING ON VELOCITY   =1.
        /
        IMPLICITATION FOR DEPTH =0.6
        /
        MASS-LUMPING ON H            =1.
        /
        IMPLICITATION FOR VELOCITY =0.6
        ```
      * .poi file (dedicated to specify Funz output Points Of Interest):
        ```
        xylowercenter=2500,15
        xymediumcenter=2500,250
        xylowerright=2900,15
        xymediumright=2900,250
        ```
      * will identify input:
        * vel, expected to default value 0.005
        * init_elev, expected to vary inside [1.5,2.5]
  * Output
    * file type supported: *.slf `RESULTS FILE` (Seraphin format)
    * read any `VARIABLES FOR GRAPHIC PRINTOUTS` at .poi points, along time
    * will return time dependant arrays like: `H_xylowerright`=`[2.3383100032806396,2.3411474227905273,2.3438708782196045,2.3465609550476074,...]`



![Analytics](https://ga-beacon.appspot.com/UA-109580-20/plugin-Telemac)
