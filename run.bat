@echo off
REM Simple run script for Task Manager Application

setlocal enabledelayedexpansion

REM Set colors
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "BLUE=[94m"
set "RESET=[0m"

echo %BLUE%Starting Task Manager Application...%RESET%
echo.

REM Set project directory
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

REM Check if build directory exists
if not exist "build" (
    echo %RED%Build directory not found!%RESET%
    echo %YELLOW%Please run build.bat first to compile the application%RESET%
    pause
    exit /b 1
)

REM Find MySQL Connector JAR
set "MYSQL_JAR="
for %%f in ("lib\mysql-connector-*.jar") do (
    set "MYSQL_JAR=%%f"
)

if "%MYSQL_JAR%"=="" (
    echo %RED%MySQL Connector/J not found in lib directory%RESET%
    echo %YELLOW%Please run build.bat first to download dependencies%RESET%
    pause
    exit /b 1
)

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo %RED%Java is not installed or not in PATH%RESET%
    pause
    exit /b 1
)

REM Run the application
echo %GREEN%Launching Task Manager...%RESET%
echo.
java -cp "build;%MYSQL_JAR%" Main

echo.
echo %YELLOW%Application closed.%RESET%
pause