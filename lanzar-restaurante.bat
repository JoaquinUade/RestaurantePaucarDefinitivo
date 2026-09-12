@echo off
setlocal

REM Ruta base del proyecto
set BASE_DIR=%~dp0

REM Inicia el backend en una nueva ventana
start "Backend Restaurante" cmd /k "cd /d "%BASE_DIR%backend" && java -jar target\demo-0.0.1-SNAPSHOT-exec.jar"

REM Espera un momento para que el backend levante
ping 127.0.0.1 -n 5 > nul

REM Inicia la interfaz JavaFX usando module-path para evitar el error de JavaFX runtime components
start "Restaurante Paucar" cmd /k "cd /d "%BASE_DIR%Intento Interfaz" && java --module-path "target\dependency" --add-modules javafx.controls,javafx.fxml -cp "target\classes;target\dependency\*" paucar.Aplicacion"

exit /b 0
