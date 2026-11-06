@echo off
rem Starts YaBS. Needs Java 21 or newer (java on the PATH or JAVA_HOME set).
cd /d "%~dp0"
set "JAVA=javaw"
set "JAVACON=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\javaw"
if defined JAVA_HOME set "JAVACON=%JAVA_HOME%\bin\java"
"%JAVA%" -version >nul 2>&1
if errorlevel 1 (
    echo Java not found. Please install Java 21 or newer, e.g. https://adoptium.net
    pause
    exit /b 1
)
set "JVERSION=0"
for /f "tokens=3" %%v in ('"%JAVACON%" -version 2^>^&1 ^| findstr /i version') do set "JVERSION=%%~v"
for /f "delims=." %%m in ("%JVERSION%") do set "JMAJOR=%%m"
if %JMAJOR% LSS 21 (
    echo YaBS needs Java 21 or newer, found Java %JVERSION%. Please install it, e.g. https://adoptium.net
    pause
    exit /b 1
)
start "" "%JAVA%" -jar openyabs.jar %*
