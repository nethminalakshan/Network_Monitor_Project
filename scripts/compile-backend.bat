@echo off
setlocal

for %%i in ("%~dp0..") do set "ROOT=%%~fi\"
set "SRC=%ROOT%backend\src\main\java"
set "OUT=%ROOT%backend\out"

echo ========================================
echo   Compiling Network Monitor Backend
echo ========================================
echo.

if exist "%OUT%" rd /s /q "%OUT%"
mkdir "%OUT%" >nul 2>nul

pushd "%SRC%"
echo Compiling Java sources...
javac -d "%OUT%" -cp "%OUT%" ^
    com\networkmonitor\model\Device.java ^
    com\networkmonitor\discovery\DeviceDiscovery.java ^
    com\networkmonitor\monitor\*.java ^
    com\networkmonitor\api\*.java ^
    com\networkmonitor\NetworkMonitor.java
if %errorlevel% neq 0 (
    echo.
    echo ❌ Compilation failed.
    popd
    exit /b 1
)
popd

echo.
echo ========================================
echo   Backend compiled to backend\out
echo ========================================
echo.

endlocal
