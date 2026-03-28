@echo off
setlocal

set "ROOT=%~dp0"
set "OUT=%ROOT%backend\out"

echo Testing Bandwidth Monitor...
echo.

if not exist "%OUT%\com\networkmonitor\monitor\BandwidthMonitor.class" (
	echo Compiled files not found. Compiling first...
	call "%ROOT%compile-backend.bat"
	if %errorlevel% neq 0 (
		endlocal
		exit /b 1
	)
)

pushd "%ROOT%backend"
java -cp "%OUT%" com.networkmonitor.monitor.BandwidthMonitor
popd

endlocal
