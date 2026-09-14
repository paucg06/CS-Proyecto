@echo off
title CS PROYECTO

echo Compilando...
javac src\code\*.java src\code\cypher\*java

if errorlevel 1 (
    echo.
    echo Error al compilar.
    pause
    exit /b 1
)

echo.
echo Ejecutando programa...
echo.

java -cp src\code Main

echo.
pause
