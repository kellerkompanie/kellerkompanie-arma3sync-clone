@echo off
setlocal

set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..

cd /d "%PROJECT_DIR%"

echo Building fat JAR...
call gradlew.bat customFatJar

set BUILD_JAR=%PROJECT_DIR%\build\libs\ArmA3Sync-keko-1.0.jar
set OUTPUT_JAR=%PROJECT_DIR%\ArmA3Sync-keko.jar

if exist "%BUILD_JAR%" (
    copy "%BUILD_JAR%" "%OUTPUT_JAR%" >nul
    echo.
    echo Build successful!
    echo JAR location: %OUTPUT_JAR%
    echo.
    echo Run with: java -jar "%OUTPUT_JAR%"
) else (
    echo Build failed - JAR not found
    exit /b 1
)
