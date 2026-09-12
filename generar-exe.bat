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
    echo Instala JDK 21 y asegúrate de que jpackage este disponible.
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

if exist "%ICON_FILE%" (
    echo Generando exe con icono...
    jpackage --type exe ^
      --name RestaurantePaucar ^
      --input target ^
      --main-jar interfaz-1.0.0-shaded.jar ^
      --main-class paucar.Aplicacion ^
      --dest "%DIST_DIR%" ^
      --module-path target\dependency ^
      --add-modules javafx.controls,javafx.fxml ^
      --icon "%ICON_FILE%" ^
      --win-console false
) else (
    echo Generando exe sin icono...
    jpackage --type exe ^
      --name RestaurantePaucar ^
      --input target ^
      --main-jar interfaz-1.0.0-shaded.jar ^
      --main-class paucar.Aplicacion ^
      --dest "%DIST_DIR%" ^
      --module-path target\dependency ^
      --add-modules javafx.controls,javafx.fxml ^
      --win-console false
)

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: fallo al generar el exe.
    pause
    exit /b 1
)

echo.
echo EXE generado correctamente.
echo Ruta: "%DIST_DIR%\RestaurantePaucar.exe"
if exist "%ICON_FILE%" (
    echo Icono usado: "%ICON_FILE%"
)

pause
