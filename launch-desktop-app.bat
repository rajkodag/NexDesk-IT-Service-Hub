@echo off
title NexDesk 2.0 — Desktop Application Launcher
echo ==============================================================================
echo             NexDesk 2.0 -- Autonomous IT Service Management
echo                Launching Desktop Standalone Application...
echo ==============================================================================

:: Check if server is running on port 8080
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] NexDesk backend server not detected on port 8080.
    echo [INFO] Starting backend server in background...
    start "NexDesk Backend" /B cmd /c "run.bat"
    echo [INFO] Waiting 8 seconds for Spring Boot server to initialize...
    timeout /t 8 /nobreak >nul
)

:: Launch in native application mode (borderless standalone window)
where msedge >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Launching NexDesk in Microsoft Edge App Mode...
    start msedge --app=http://localhost:8080/
    exit /b 0
)

where chrome >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Launching NexDesk in Google Chrome App Mode...
    start chrome --app=http://localhost:8080/
    exit /b 0
)

echo [INFO] Opening default system browser at http://localhost:8080/
start http://localhost:8080/
exit /b 0
