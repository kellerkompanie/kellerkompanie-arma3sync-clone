@echo off
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..

if "%~1"=="" (
    echo Usage: %0 ^<ssh-host^>
    echo Example: %0 hostname
    exit /b 1
)

set SSH_HOST=%~1

:: Find the .deb file
set DEB_FILE=
for %%f in ("%PROJECT_DIR%\dist\keko-arma3sync_*.deb") do (
    set DEB_FILE=%%f
    set DEB_NAME=%%~nxf
)

if "%DEB_FILE%"=="" (
    echo Error: No .deb file found in dist\
    echo Run scripts\build-deb.bat first
    exit /b 1
)

echo Deploying %DEB_NAME% to %SSH_HOST%...

echo Copying package...
scp "%DEB_FILE%" "%SSH_HOST%:/tmp/%DEB_NAME%"
if errorlevel 1 exit /b 1

echo Installing package...
ssh "%SSH_HOST%" "sudo dpkg -i /tmp/%DEB_NAME% && rm /tmp/%DEB_NAME%"
if errorlevel 1 exit /b 1

echo.
echo Deployment successful!
echo Run 'keko-arma3sync -buildall' on %SSH_HOST% to build all repositories

endlocal
