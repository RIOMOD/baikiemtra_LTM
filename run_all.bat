@echo off
chcp 65001 > nul
cls
echo ============================================================
echo   PTIT - BAI KIEM TRA LAP TRINH MANG
echo   DE TAI: CHAT VA TRUYEN FILE DA LUONG TCP (JAVA SWING)
echo   Giang vien: Thay Van Tong Thanh
echo ============================================================

cd /d "%~dp0"

if not exist bin mkdir bin

echo [1/2] Dang bien dich ma nguon Java...
javac -encoding UTF-8 -d bin src/common/*.java src/server/*.java src/client/*.java src/*.java
if %errorlevel% neq 0 (
    echo [LOI] Bien dich that bai!
    pause
    exit /b %errorlevel%
)

echo [2/2] Dang khoi chay Bo dieu khien trung tam...
java -cp bin MainLauncher
if %errorlevel% neq 0 (
    pause
)
