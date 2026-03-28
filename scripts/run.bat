@echo off
setlocal

for %%i in ("%~dp0..") do set "ROOT=%%~fi\"
set "OUT=%ROOT%backend\out"

echo ========================================
echo   Network Monitor - Launch Script
echo ========================================
echo.

if not exist "%OUT%\com\networkmonitor\NetworkMonitor.class" (
    echo Compiled files not found. Compiling first...
    call "%ROOT%compile-backend.bat"
    if %errorlevel% neq 0 (
        echo Compilation failed. Please check for errors.
        endlocal
        exit /b 1
    )
)

echo Starting Network Monitor...
echo.
echo ----------------------------------------
echo   IMPORTANT NOTES:
echo ----------------------------------------
echo 1. Run as Administrator for full features
echo 2. Ensure firewall allows ICMP and ARP
echo 3. Press Ctrl+C to stop monitoring
echo ----------------------------------------
echo.

pushd "%ROOT%backend"
java -cp "%OUT%" com.networkmonitor.NetworkMonitor
popd

endlocal
