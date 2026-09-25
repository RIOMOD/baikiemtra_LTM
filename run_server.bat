@echo off
chcp 65001 > nul
cls
echo ============================================================
echo   KHOI CHAY TCP CHAT SERVER CONTROL CENTER
echo ============================================================

cd /d "%~dp0"
if not exist bin\server\ServerGUI.class (
    echo Dang bien dich...
    javac -encoding UTF-8 -d bin src/common/*.java src/server/*.java src/client/*.java src/*.java
)

java -cp bin server.ServerGUI
if %errorlevel% neq 0 (
    pause
)
