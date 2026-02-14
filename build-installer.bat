@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Gestion Clients - Build Tool
echo ========================================

echo 0. Cleaning dist folder...
if exist dist rmdir /s /q dist
mkdir dist
mkdir dist\installer

echo 1. Building JAR (skipping clean to avoid locks)...
call .\mvnw.cmd package -DskipTests
if !ERRORLEVEL! NEQ 0 (
    echo Maven build failed.
    exit /b
)

echo.
echo 2. Preparing Environment (Adding WiX to PATH)...
set "PATH=%PATH%;C:\Program Files (x86)\WiX Toolset v3.11\bin"

echo.
echo 3. Creating Portable App Image (.exe inside folder)...
"%JAVA_HOME%\bin\jpackage" ^
  --name "GestionClients" ^
  --input target\libs ^
  --main-jar gestion_cleints-1.0-SNAPSHOT.jar ^
  --main-class tp.gestion_cleints.Launcher ^
  --type app-image ^
  --dest dist\portable ^
  --vendor "TP" ^
  --app-version 1.0 ^
  --win-console ^
  --icon logo.ico ^
  --verbose

if !ERRORLEVEL! EQU 0 (
    echo [SUCCESS] Portable version created in dist\portable\GestionClients
)

echo.
echo 4. Attempting to create EXE Installer...
"%JAVA_HOME%\bin\jpackage" ^
  --name "GestionClients_Setup" ^
  --input target\libs ^
  --main-jar gestion_cleints-1.0-SNAPSHOT.jar ^
  --main-class tp.gestion_cleints.Launcher ^
  --type exe ^
  --win-dir-chooser ^
  --win-shortcut ^
  --dest dist\installer ^
  --vendor "TP" ^
  --description "Client Management Application" ^
  --app-version 1.0 ^
  --icon logo.ico ^
  --verbose

if !ERRORLEVEL! EQU 0 (
    echo [SUCCESS] EXE Installer created in dist\installer
)

echo.
echo 5. Attempting to create MSI Installer (Alternative)...
"%JAVA_HOME%\bin\jpackage" ^
  --name "GestionClients_MSI" ^
  --input target\libs ^
  --main-jar gestion_cleints-1.0-SNAPSHOT.jar ^
  --main-class tp.gestion_cleints.Launcher ^
  --type msi ^
  --win-dir-chooser ^
  --win-shortcut ^
  --dest dist\installer ^
  --vendor "TP" ^
  --description "Client Management Application" ^
  --app-version 1.0 ^
  --icon logo.ico ^
  --verbose

if !ERRORLEVEL! EQU 0 (
    echo [SUCCESS] MSI Installer created in dist\installer
) else (
    echo [INFO] Installer creation failed. Check verbose output above.
)

echo.
echo Operation Completed.
