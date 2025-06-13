@echo off
REM Task Manager Application Build Script
REM This script compiles and runs the Java Task Manager application

setlocal enabledelayedexpansion

REM Set colors for output (using simple text markers)
set "RED=[ERROR]"
set "GREEN=[SUCCESS]"
set "YELLOW=[INFO]"
set "BLUE=[BUILD]"
set "RESET="

echo [BUILD]========================================
echo [BUILD]    Task Manager Application Builder    
echo [BUILD]========================================
echo.

REM Java installation check skipped - assuming Java is installed
echo [INFO]Skipping Java installation check...
echo [SUCCESS]Proceeding with build...
echo.

REM Set project directories
set "PROJECT_DIR=%~dp0"
set "SRC_DIR=%PROJECT_DIR%src"
set "BUILD_DIR=%PROJECT_DIR%build"
set "LIB_DIR=%PROJECT_DIR%lib"
set "DIST_DIR=%PROJECT_DIR%dist"

REM Create necessary directories
echo [INFO]Creating build directories...
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
if not exist "%PROJECT_DIR%logs" mkdir "%PROJECT_DIR%logs"

REM Check for MySQL Connector/J
set "MYSQL_JAR="
for %%f in ("%LIB_DIR%\mysql-connector-*.jar") do (
    set "MYSQL_JAR=%%f"
)

if "%MYSQL_JAR%"=="" (
    echo [INFO]MySQL Connector/J not found in lib directory
    echo [INFO]Attempting to download MySQL Connector/J...
    
    REM Try to download MySQL Connector/J using PowerShell
    powershell -Command "& {try { Invoke-WebRequest -Uri 'https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-j-8.2.0.jar' -OutFile '%LIB_DIR%\mysql-connector-j-8.2.0.jar' -UseBasicParsing; Write-Host 'Download completed successfully' } catch { Write-Host 'Download failed. Please download manually.' }}"
    
    REM Check if download was successful
    for %%f in ("%LIB_DIR%\mysql-connector-*.jar") do (
        set "MYSQL_JAR=%%f"
    )
    
    if "%MYSQL_JAR%"=="" (
        echo [ERROR]MySQL Connector/J is required but not found
        echo Please download MySQL Connector/J from:
        echo https://dev.mysql.com/downloads/connector/j/
        echo And place the JAR file in the lib directory
        pause
        exit /b 1
    )
)

echo [SUCCESS]MySQL Connector/J found: %MYSQL_JAR%
echo.

REM Set classpath
set "CLASSPATH=%SRC_DIR%;%MYSQL_JAR%"

REM Clean previous build
echo [INFO]Cleaning previous build...
if exist "%BUILD_DIR%\*.class" del /q "%BUILD_DIR%\*.class"
for /d %%d in ("%BUILD_DIR%\*") do rmdir /s /q "%%d"

REM Find all Java files
echo [INFO]Finding Java source files...
set "JAVA_FILES="
for /r "%SRC_DIR%" %%f in (*.java) do (
    set "JAVA_FILES=!JAVA_FILES! "%%f""
)

if "%JAVA_FILES%"=="" (
    echo [ERROR]No Java source files found in %SRC_DIR%
    pause
    exit /b 1
)

echo [SUCCESS]Found Java source files
echo.

REM Compile Java files
echo [INFO]Compiling Java source files...
javac -cp "%CLASSPATH%" -d "%BUILD_DIR%" %JAVA_FILES%

if errorlevel 1 (
    echo [ERROR]Compilation failed!
    pause
    exit /b 1
)

echo [SUCCESS]Compilation successful!
echo.

REM Copy resources
echo [INFO]Copying resources...
if exist "%PROJECT_DIR%\database.properties" (
    copy "%PROJECT_DIR%\database.properties" "%BUILD_DIR%\" >nul
    echo [SUCCESS]Copied database.properties
)

if exist "%PROJECT_DIR%\*.sql" (
    copy "%PROJECT_DIR%\*.sql" "%BUILD_DIR%\" >nul
    echo [SUCCESS]Copied SQL files
)

REM Create manifest file
echo [INFO]Creating manifest file...
echo Main-Class: Main > "%BUILD_DIR%\MANIFEST.MF"
echo Class-Path: . %MYSQL_JAR% >> "%BUILD_DIR%\MANIFEST.MF"
echo. >> "%BUILD_DIR%\MANIFEST.MF"

REM Create JAR file
echo [INFO]Creating JAR file...
cd /d "%BUILD_DIR%"
jar cfm "%DIST_DIR%\TaskManager.jar" MANIFEST.MF *.class dao\*.class models\*.class interfaces\*.class gui\*.class database\*.class *.properties *.sql 2>nul

if errorlevel 1 (
    echo [INFO]JAR creation failed, but application can still run from build directory
) else (
    echo [SUCCESS]JAR file created: %DIST_DIR%\TaskManager.jar
)

cd /d "%PROJECT_DIR%"
echo.

REM Create run script
echo [INFO]Creating run script...
echo @echo off > "%PROJECT_DIR%\run.bat"
echo cd /d "%PROJECT_DIR%" >> "%PROJECT_DIR%\run.bat"
echo java -cp "build;%MYSQL_JAR%" Main >> "%PROJECT_DIR%\run.bat"
echo pause >> "%PROJECT_DIR%\run.bat"

echo [SUCCESS]Run script created: run.bat
echo.

REM Ask user if they want to run the application
echo [BUILD]Build completed successfully!
echo.
set /p "choice=Do you want to run the application now? (y/n): "
if /i "%choice%"=="y" (
    echo [INFO]Starting Task Manager Application...
    echo.
    java -cp "build;%MYSQL_JAR%" Main
) else (
    echo [SUCCESS]You can run the application later using:
echo   run.bat
echo   or
echo   java -cp "build;%MYSQL_JAR%" Main
)

echo.
echo [BUILD]Build script completed!
pause