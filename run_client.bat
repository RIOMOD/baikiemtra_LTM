@echo off
chcp 65001 > nul
cls
echo ============================================================
echo   KHOI CHAY TCP CHAT VA FILE CLIENT
echo ============================================================

cd /d "%~dp0"
if not exist bin\client\ClientGUI.class (
    echo Dang bien dich...
    javac -encoding UTF-8 -d bin src/common/*.java src/server/*.java src/client/*.java src/*.java
)

java -cp bin client.ClientGUI
if %errorlevel% neq 0 (
    pause
)
