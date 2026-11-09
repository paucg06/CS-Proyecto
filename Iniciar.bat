@echo off
title CS PROYECTO

echo Compilando...
javac -d cliente\src\code cliente\src\code\*.java cliente\src\code\cypher\*.java cliente\src\code\ui\*.java

if errorlevel 1 (
    echo.
    echo Error al compilar.
    pause
    exit /b 1
)

echo.
echo Ejecutando programa...
echo.

start "" javaw -cp cliente\src\code ui.Prueba
