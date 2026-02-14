@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Gestion Clients - 32-bit Build Tool
echo ========================================

echo 0. Cleaning dist_32bit folder...
if exist dist\installer_32bit rmdir /s /q dist\installer_32bit
mkdir dist\installer_32bit

:: Set 32-bit JDK and WiX paths
set "JAVA_HOME=C:\zulu17.64.17-ca-fx-jdk17.0.18-win_i686"
set "PATH=%JAVA_HOME%\bin;%PATH%;C:\Program Files (x86)\WiX Toolset v3.11\bin"

echo 1. Verifying JAR availability...
if not exist target\libs\gestion_cleints-1.0-SNAPSHOT.jar (
    echo [ERROR] Application JAR not found in target\libs folder. 
    echo Please run build-installer.bat first to build the 64-bit JAR.
    exit /b
)

echo.
echo 2. Creating 32-bit MSI Installer...
echo (Using existing JAR and 32-bit Zulu FX JMODS)
"%JAVA_HOME%\bin\jpackage" ^
  --name "GestionClients_32bit" ^
  --input target\libs ^
  --main-jar gestion_cleints-1.0-SNAPSHOT.jar ^
  --main-class tp.gestion_cleints.Launcher ^
  --type msi ^
  --win-dir-chooser ^
  --win-shortcut ^
  --dest dist\installer_32bit ^
  --vendor "TP" ^
  --description "Client Management Application (32-bit)" ^
  --app-version 1.0 ^
  --module-path "%JAVA_HOME%\jmods" ^
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.web ^
  --icon logo.ico ^
  --verbose

if !ERRORLEVEL! EQU 0 (
    echo [SUCCESS] 32-bit MSI Installer created in dist\installer_32bit
) else (
    echo [ERROR] 32-bit MSI creation failed.
)

echo.
echo Operation Completed.
