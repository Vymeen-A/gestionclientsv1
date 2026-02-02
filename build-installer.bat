@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Gestion Clients - Build Tool
echo ========================================

echo 1. Building JAR (skipping clean to avoid locks)...
call mvnw.cmd package -DskipTests
if !ERRORLEVEL! NEQ 0 (
    echo Maven build failed.
    pause
    exit /b
)

echo.
echo 2. Creating Portable App Image (.exe inside folder)...
"%JAVA_HOME%\bin\jpackage" ^
  --name "GestionClients" ^
  --input target\libs ^
  --main-jar gestion_cleints-1.0-SNAPSHOT.jar ^
  --main-class tp.gestion_cleints.Launcher ^
  --type app-image ^
  --dest target\portable ^
  --vendor "TP" ^
  --app-version 1.0

if !ERRORLEVEL! EQU 0 (
    echo [SUCCESS] Portable version created in target\portable\GestionClients
)

echo.
echo 3. Attempting to create EXE Installer...
echo (Note: This requires WiX Toolset v3.11+ installed)
"%JAVA_HOME%\bin\jpackage" ^
  --name "GestionClientsSetup" ^
  --input target\libs ^
  --main-jar gestion_cleints-1.0-SNAPSHOT.jar ^
  --main-class tp.gestion_cleints.Launcher ^
  --type exe ^
  --win-dir-chooser ^
  --win-shortcut ^
  --dest target\installer ^
  --vendor "TP" ^
  --description "Client Management Application" ^
  --app-version 1.0

if !ERRORLEVEL! EQU 0 (
    echo [SUCCESS] Installer created in target\installer
) else (
    echo [INFO] Installer creation skipped or failed (likely WiX is missing).
    echo You can still use the portable version in target\portable\GestionClients
)

echo.
echo Operation Completed.
pause
