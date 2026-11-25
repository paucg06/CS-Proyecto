@echo off
title CS PROYECTO

echo Compilando...
javac -encoding UTF-8 -d cliente\src\code cliente\src\code\*.java cliente\src\code\cypher\*.java cliente\src\code\ui\*.java cliente\src\code\ui\components\*.java cliente\src\service\*.java

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
