@echo on

CAS=%1%

NCPU=%NUMBER_OF_PROCESSORS%

rem assume:
rem  1. WSL2 is installed on your Windows: "wsl.exe --install", then reboot, then "wsl.exe --set-defaut-version 2"
rem  2. Debian distro is installed in WSL2: "wsl.exe --install -d Debian"
rem  3. if behind proxy, within Debian: "export https_proxy="http://login:passwd@proxy-ip:port", and "docker pull irsn/telemac-mascaret" to pre-install Telemac.
rem Then, following run should work:
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
