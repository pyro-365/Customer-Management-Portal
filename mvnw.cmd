@echo off
setlocal
if not defined JAVA_HOME (
    if exist "%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe" (
        set "JAVA_HOME=%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64"
    ) else if exist "D:\bin\java.exe" (
        set "JAVA_HOME=D:\"
    )
)
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

set "MAVEN_CMD=mvn"
if exist "D:\apache-maven-3.10.0\bin\mvn.cmd" (
    set "MAVEN_CMD=D:\apache-maven-3.10.0\bin\mvn.cmd"
) else if exist "%USERPROFILE%\.m2\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MAVEN_CMD=%USERPROFILE%\.m2\apache-maven-3.9.9\bin\mvn.cmd"
)

if "%1"=="spring-boot:run" (
    call "%MAVEN_CMD%" package -DskipTests
    if errorlevel 1 exit /b %errorlevel%
    echo Starting Customer Management System on http://localhost:8080 ...
    "D:\bin\java.exe" -jar "%~dp0target\customer-management-web-1.0.0.jar"
    exit /b %errorlevel%
)

call "%MAVEN_CMD%" %*

