@echo off
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..
set IMAGE_NAME=keko-arma3sync-builder

cd /d "%PROJECT_DIR%"

echo Building Docker image...
docker build -t %IMAGE_NAME% -f docker/Dockerfile .

echo.
echo Building Debian package...
if not exist "%PROJECT_DIR%\dist" mkdir "%PROJECT_DIR%\dist"
docker run --rm -v "%PROJECT_DIR%:/build" -v "%PROJECT_DIR%/dist:/dist" -w /build %IMAGE_NAME% sh -c "dpkg-buildpackage -us -uc -b && cp /*.deb /dist/ && dh clean"

echo.
for %%f in ("%PROJECT_DIR%\dist\keko-arma3sync_*.deb") do (
    echo Build successful!
    echo Package: %%f
    echo.
    echo Install with: sudo dpkg -i %%f
    goto :done
)

echo Build failed - .deb file not found
exit /b 1

:done
endlocal
