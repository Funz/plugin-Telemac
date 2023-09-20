@echo on

CAS=%1%

NCPU=%NUMBER_OF_PROCESSORS%

rem assume proxy is ok (export https_proxy="http://login:passwd@proxy-ip:port"), or docker pull irsn/telemac-mascaret was already done.
wsl -u root -e bash -c "docker run -v `echo $PWD`:/workdir irsn/telemac-mascaret:latest telemac2d.py --ncsize=%NCPU% %CAS%"

for /F "TOKENS=1,2,*" %%a in ('tasklist /FI "IMAGENAME eq wsl.exe"') do set PID_WSL=%%b
echo %PID_WSL% > PID

:loop
tasklist | find " %PID_WSL% " >nul
if not errorlevel 1 (
    timeout /t 1 >nul
    goto :loop
)

del /f PID
