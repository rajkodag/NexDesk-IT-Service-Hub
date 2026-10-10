@echo off
title NexDesk 2.0 — Android APK Packaging & Build Wizard
echo ==============================================================================
echo             NexDesk 2.0 -- Autonomous IT Service Management
echo               Android APK ^& Google Play Store Build Wizard
echo ==============================================================================
echo.
echo This wizard packages NexDesk 2.0 into an installable Android APK / AAB
echo using Google's official Bubblewrap (Trusted Web Activity) CLI.
echo.
echo Requirements:
echo  - Node.js / npx (installed)
echo  - Java JDK 17+ or 21+
echo  - Android SDK (automatically downloaded by bubblewrap if missing)
echo.
pause

where npx >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Node.js and npx are required to run Google Bubblewrap.
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

echo.
echo [STEP 1/3] Checking Bubblewrap CLI...
call npx -y @bubblewrap/cli --version
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to execute @bubblewrap/cli. Check your internet connection.
    pause
    exit /b 1
)

echo.
echo [STEP 2/3] Initializing Android Project Directory (android-build)...
if not exist "android-build" mkdir android-build

copy /y "android\twa-manifest.json" "android-build\twa-manifest.json" >nul

cd android-build

echo.
echo [STEP 3/3] Building signed Android APK & AAB...
echo Note: If prompted, accept the default values or provide your Android keystore credentials.
call npx @bubblewrap/cli build

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==============================================================================
    echo [SUCCESS] Android APK built successfully!
    echo Located in: android-build\app-release-signed.apk
    echo To install on your connected phone:
    echo    adb install android-build\app-release-signed.apk
    echo ==============================================================================
) else (
    echo.
    echo [NOTICE] If build requires keystore setup, run:
    echo    cd android-build
    echo    npx @bubblewrap/cli init --manifest=http://localhost:8080/manifest.json
    echo    npx @bubblewrap/cli build
)

cd ..
pause
