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

echo %BLUE%========================================%RESET%
echo %BLUE%    Task Manager Application Builder    %RESET%
echo %BLUE%========================================%RESET%
echo.

REM Check if Java is installed
echo %YELLOW%Checking Java installation...%RESET%
java -version >nul 2>nul
if errorlevel 1 (
    echo %RED%Error: Java is not installed or not in PATH%RESET%
    echo Please install Java JDK 17 or higher
    pause
    exit /b 1
)

javac -version >nul 2>nul
if errorlevel 1 (
    echo %RED%Error: Java compiler (javac) is not installed or not in PATH%RESET%
    echo Please install Java JDK 17 or higher
    pause
    exit /b 1
)

echo %GREEN%Java installation found!%RESET%
echo.

REM Set project directories
set "PROJECT_DIR=%~dp0"
set "SRC_DIR=%PROJECT_DIR%src"
set "BUILD_DIR=%PROJECT_DIR%build"
set "LIB_DIR=%PROJECT_DIR%lib"
set "DIST_DIR=%PROJECT_DIR%dist"

REM Create necessary directories
echo %YELLOW%Creating build directories...%RESET%
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
    echo %YELLOW%MySQL Connector/J not found in lib directory%RESET%
    echo %YELLOW%Attempting to download MySQL Connector/J...%RESET%
    
    REM Try to download MySQL Connector/J using PowerShell
    powershell -Command "& {try { Invoke-WebRequest -Uri 'https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-j-8.2.0.jar' -OutFile '%LIB_DIR%\mysql-connector-j-8.2.0.jar' -UseBasicParsing; Write-Host 'Download completed successfully' } catch { Write-Host 'Download failed. Please download manually.' }}"
    
    REM Check if download was successful
    for %%f in ("%LIB_DIR%\mysql-connector-*.jar") do (
        set "MYSQL_JAR=%%f"
    )
    
    if "%MYSQL_JAR%"=="" (
        echo %RED%Error: MySQL Connector/J is required but not found%RESET%
        echo Please download MySQL Connector/J from:
        echo https://dev.mysql.com/downloads/connector/j/
        echo And place the JAR file in the lib directory
        pause
        exit /b 1
    )
)

echo %GREEN%MySQL Connector/J found: %MYSQL_JAR%%RESET%
echo.

REM Set classpath
set "CLASSPATH=%SRC_DIR%;%MYSQL_JAR%"

REM Clean previous build
echo %YELLOW%Cleaning previous build...%RESET%
if exist "%BUILD_DIR%\*.class" del /q "%BUILD_DIR%\*.class"
for /d %%d in ("%BUILD_DIR%\*") do rmdir /s /q "%%d"

REM Find all Java files
echo %YELLOW%Finding Java source files...%RESET%
set "JAVA_FILES="
for /r "%SRC_DIR%" %%f in (*.java) do (
    set "JAVA_FILES=!JAVA_FILES! "%%f""
)

if "%JAVA_FILES%"=="" (
    echo %RED%Error: No Java source files found in %SRC_DIR%%RESET%
    pause
    exit /b 1
)

echo %GREEN%Found Java source files%RESET%
echo.

REM Compile Java files
echo %YELLOW%Compiling Java source files...%RESET%
javac -cp "%CLASSPATH%" -d "%BUILD_DIR%" %JAVA_FILES%

if errorlevel 1 (
    echo %RED%Compilation failed!%RESET%
    pause
    exit /b 1
)

echo %GREEN%Compilation successful!%RESET%
echo.

REM Copy resources
echo %YELLOW%Copying resources...%RESET%
if exist "%PROJECT_DIR%\database.properties" (
    copy "%PROJECT_DIR%\database.properties" "%BUILD_DIR%\" >nul
    echo %GREEN%Copied database.properties%RESET%
)

if exist "%PROJECT_DIR%\*.sql" (
    copy "%PROJECT_DIR%\*.sql" "%BUILD_DIR%\" >nul
    echo %GREEN%Copied SQL files%RESET%
)

REM Create manifest file
echo %YELLOW%Creating manifest file...%RESET%
echo Main-Class: Main > "%BUILD_DIR%\MANIFEST.MF"
echo Class-Path: . %MYSQL_JAR% >> "%BUILD_DIR%\MANIFEST.MF"
echo. >> "%BUILD_DIR%\MANIFEST.MF"

REM Create JAR file
echo %YELLOW%Creating JAR file...%RESET%
cd /d "%BUILD_DIR%"
jar cfm "%DIST_DIR%\TaskManager.jar" MANIFEST.MF *.class dao\*.class models\*.class interfaces\*.class gui\*.class database\*.class *.properties *.sql 2>nul

if errorlevel 1 (
    echo %YELLOW%JAR creation failed, but application can still run from build directory%RESET%
) else (
    echo %GREEN%JAR file created: %DIST_DIR%\TaskManager.jar%RESET%
)

cd /d "%PROJECT_DIR%"
echo.

REM Create run script
echo %YELLOW%Creating run script...%RESET%
echo @echo off > "%PROJECT_DIR%\run.bat"
echo cd /d "%PROJECT_DIR%" >> "%PROJECT_DIR%\run.bat"
echo java -cp "build;%MYSQL_JAR%" Main >> "%PROJECT_DIR%\run.bat"
echo pause >> "%PROJECT_DIR%\run.bat"

echo %GREEN%Run script created: run.bat%RESET%
echo.

REM Ask user if they want to run the application
echo %BLUE%Build completed successfully!%RESET%
echo.
set /p "choice=Do you want to run the application now? (y/n): "
if /i "%choice%"=="y" (
    echo %YELLOW%Starting Task Manager Application...%RESET%
    echo.
    java -cp "build;%MYSQL_JAR%" Main
) else (
    echo %GREEN%You can run the application later using:%RESET%
    echo   %YELLOW%run.bat%RESET%
    echo   or
    echo   %YELLOW%java -cp "build;%MYSQL_JAR%" Main%RESET%
)

echo.
echo %BLUE%Build script completed!%RESET%
pause