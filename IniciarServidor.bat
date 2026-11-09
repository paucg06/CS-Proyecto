@echo off
title CS PROYECTO - SERVIDOR
color 0A

echo ========================================================
echo        CS-PROYECTO: SERVIDOR LOCAL (SQLite + HTTP)
echo ========================================================
echo.
echo [1/2] Compilando servidor y librerias...

javac -encoding UTF-8 -cp "servidor\lib\*" servidor\src\*.java

if errorlevel 1 (
    echo.
    color 0C
    echo [ERROR] Fallo la compilacion del servidor.
    echo Revisa que los archivos en servidor\src no tengan errores de sintaxis.
    echo.
    pause
    exit /b 1
)

echo [OK] Compilacion completada con exito.
echo.
echo [2/2] Iniciando servidor en http://localhost:8080 ...
echo Presiona Ctrl+C en esta consola para detener el servidor.
echo.

java -cp "servidor\src;servidor\lib\*" Main

if errorlevel 1 (
    echo.
    color 0C
    echo [ERROR] El servidor se ha detenido inesperadamente.
    echo.
    pause
)
