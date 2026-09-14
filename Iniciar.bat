@echo off
title CS PROYECTO

echo Compilando...
javac src\code\Main.java

if errorlevel 1 (
    echo.
    echo Error al compilar.
    pause
    exit /b 1
)

echo.
echo Ejecutando programa...
echo.

java -cp src Main

echo.
pause
