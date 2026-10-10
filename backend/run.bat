@echo off
setlocal

title NexDesk Enterprise IT Service Management Platform

echo ==================================================================
echo       NexDesk Enterprise IT Service Management Platform
echo       Spring Boot 3 + Oracle Database + Spring Security + JWT
echo ==================================================================
echo.

cd /d "%~dp0"

set "MAVEN_CMD=mvn"
where mvn >nul 2>&1
if errorlevel 1 (
    if exist "%USERPROFILE%\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd" (
        set "MAVEN_CMD=%USERPROFILE%\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd"
    ) else if exist "C:\Users\rajko\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd" (
        set "MAVEN_CMD=C:\Users\rajko\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd"
    ) else (
        echo [ERROR] Maven not found on PATH or in Downloads directory.
        echo Please ensure Apache Maven is installed.
        pause
        exit /b 1
    )
)

set "MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT"

echo [INFO] Using Maven: %MAVEN_CMD%
echo [INFO] Starting Spring Boot application on port 8080...
echo [INFO] Web Portal:    http://localhost:8080/
echo [INFO] Swagger Docs:  http://localhost:8080/swagger-ui/index.html
echo.

call "%MAVEN_CMD%" spring-boot:run

if errorlevel 1 (
    echo.
    echo ==================================================================
    echo       APPLICATION EXECUTION FAILED
    echo ==================================================================
    echo.
    pause
    exit /b 1
)

endlocal