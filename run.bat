@echo off
setlocal enabledelayedexpansion
title Customer Management Web Portal
cd /d "%~dp0"

cls
echo ======================================================================
echo             CUSTOMER MANAGEMENT WEB PORTAL (SUPABASE + AI)
echo ======================================================================
echo.

:: 1. Verify .env exists
if not exist ".env" (
    if exist ".env.example" (
        echo [!] .env not found. Creating from .env.example ...
        copy ".env.example" ".env" >nul
        echo [!] Please edit .env with your SUPABASE_DB_URL and GEMINI_API_KEY.
        echo.
    ) else (
        echo [ERROR] .env file is missing.
        pause
        exit /b 1
    )
)

:: 2. Locate Java 21 runtime
set "JAVA_CMD="

if exist "D:\bin\java.exe" (
    set "JAVA_CMD=D:\bin\java.exe"
    goto :java_found
)

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
    goto :java_found
)

if exist "%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe" (
    set "JAVA_CMD=%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe"
    goto :java_found
)

for /d %%D in ("C:\Program Files\Java\jdk-21*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_CMD=%%D\bin\java.exe"
        goto :java_found
    )
)

for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_CMD=%%D\bin\java.exe"
        goto :java_found
    )
)

set "JAVA_CMD=java"

:java_found
echo [*] Java Runtime : %JAVA_CMD%

:: 3. Build application if target jar is missing
if not exist "target\customer-management-web-1.0.0.jar" (
    echo [*] Application package missing. Building first with Maven...
    call mvnw.cmd package -DskipTests
    if errorlevel 1 (
        echo.
        echo [ERROR] Maven build failed. Check error messages above.
        pause
        exit /b 1
    )
)

:: 4. Automatically open browser once server is ready
start "" cmd /c "timeout /t 5 /nobreak >nul & start http://localhost:8080/index.html"

echo [*] Starting server on http://localhost:8080/index.html ...
echo [*] Your default web browser will open automatically in 5 seconds.
echo.
echo ----------------------------------------------------------------------
echo   Quick Links:
echo     - Login / Home:     http://localhost:8080/index.html
echo     - Store Catalog:    http://localhost:8080/shop.html
echo     - Customer Portal:  http://localhost:8080/dashboard.html
echo ----------------------------------------------------------------------
echo.
echo Press Ctrl+C in this window anytime to stop the server.
echo.

"%JAVA_CMD%" -jar target\customer-management-web-1.0.0.jar
if errorlevel 1 (
    echo.
    echo [ERROR] Application exited with error code %ERRORLEVEL%.
    pause
)
