@echo off
setlocal enabledelayedexpansion

set BASE_DIR=%~dp0
set APP_DIR=%BASE_DIR%Intento Interfaz
set DIST_DIR=%APP_DIR%\dist
set ICON_FILE=%BASE_DIR%app.ico

cd /d "%APP_DIR%"

where jpackage >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: jpackage no esta disponible en PATH.
    echo Instala JDK 21 y asegura que jpackage este disponible.
    pause
    exit /b 1
)

if not exist "target\interfaz-1.0.0-shaded.jar" (
    echo Compilando interfaz...
    call mvn package -DskipTests
    if %ERRORLEVEL% NEQ 0 (
        echo ERROR: fallo al compilar la interfaz.
        pause
        exit /b 1
    )
)

if not exist "target\dependency" (
    echo Copiando dependencias...
    call mvn dependency:copy-dependencies -DincludeScope=runtime -DskipTests
    if %ERRORLEVEL% NEQ 0 (
        echo ERROR: fallo al copiar dependencias.
        pause
        exit /b 1
    )
)

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

set JPACKAGE_EXE_ARGS=--type exe --name RestaurantePaucar --input target --main-jar interfaz-1.0.0-shaded.jar --main-class paucar.Aplicacion --dest "%DIST_DIR%" --module-path target\dependency --add-modules javafx.controls,javafx.fxml --win-console false
set JPACKAGE_MSI_ARGS=--type msi --name RestaurantePaucar --input target --main-jar interfaz-1.0.0-shaded.jar --main-class paucar.Aplicacion --dest "%DIST_DIR%" --module-path target\dependency --add-modules javafx.controls,javafx.fxml --win-console false

if exist "%ICON_FILE%" (
    set JPACKAGE_EXE_ARGS=!JPACKAGE_EXE_ARGS! --icon "%ICON_FILE%"
    set JPACKAGE_MSI_ARGS=!JPACKAGE_MSI_ARGS! --icon "%ICON_FILE%"
)

echo Generando ejecutable EXE...
jpackage %JPACKAGE_EXE_ARGS%
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: fallo al generar el EXE.
    pause
    exit /b 1
)

echo Generando instalador MSI...
jpackage %JPACKAGE_MSI_ARGS%
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: fallo al generar el MSI. Si no esta instalado WiX, instala WiX Toolset y vuelve a intentar.
    pause
    exit /b 1
)

echo.
echo Archivos generados correctamente:
echo   - "%DIST_DIR%\RestaurantePaucar.exe"
echo   - "%DIST_DIR%\RestaurantePaucar.msi"
if exist "%ICON_FILE%" (
    echo   - Icono usado: "%ICON_FILE%"
)
pause
