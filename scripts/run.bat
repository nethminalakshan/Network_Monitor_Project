@echo off
setlocal

for %%i in ("%~dp0..") do set "ROOT=%%~fi\"
set "OUT=%ROOT%backend\out"

:main_menu
cls
echo ========================================
echo   Network Monitor - Launch Script
echo ========================================
echo.
echo 1. Run Network Monitor
echo 2. Compile backend only
echo 3. Exit
echo.
choice /c 123 /m "Select an option"
set "opt=%errorlevel%"

if "%opt%"=="1" goto run_monitor
if "%opt%"=="2" goto compile_only
if "%opt%"=="3" goto end_script

echo Invalid choice. Try again.
timeout /t 2 >nul
goto main_menu

:compile_needed
if exist "%OUT%\com\networkmonitor\NetworkMonitor.class" goto run_monitor
echo Compiled files not found. Compiling first...
call "%ROOT%compile-backend.bat"
if %errorlevel% neq 0 (
    echo Compilation failed. Please check for errors.
    timeout /t 3 >nul
    goto main_menu
)
goto run_monitor

:compile_only
call "%ROOT%compile-backend.bat"
if %errorlevel% neq 0 (
    echo Compilation failed. Please check for errors.
    timeout /t 3 >nul
    goto main_menu
)
echo Compilation complete.
timeout /t 2 >nul
goto main_menu

:run_monitor
if not exist "%OUT%\com\networkmonitor\NetworkMonitor.class" goto compile_needed
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

echo.
echo Monitoring stopped. Returning to menu...
timeout /t 2 >nul
goto main_menu

:end_script
endlocal
exit /b 0
