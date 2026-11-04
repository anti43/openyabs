@echo off
rem Starts YaBS. Needs Java 11 or newer (java on the PATH or JAVA_HOME set).
cd /d "%~dp0"
set "JAVA=javaw"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\javaw"
"%JAVA%" -version >nul 2>&1
if errorlevel 1 (
    echo Java not found. Please install Java 11 or newer, e.g. https://adoptium.net
    pause
    exit /b 1
)
start "" "%JAVA%" -jar openyabs.jar %*
