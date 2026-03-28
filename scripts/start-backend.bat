@echo off
setlocal

for %%i in ("%~dp0..") do set "ROOT=%%~fi\"
set "OUT=%ROOT%backend\out"

echo ========================================
echo   Starting Network Monitor API Server
echo ========================================
echo.

if not exist "%OUT%\com\networkmonitor\api\APIServer.class" (
    echo Backend not compiled. Compiling now...
    call "%ROOT%compile-backend.bat"
    if %errorlevel% neq 0 (
        endlocal
        exit /b 1
    )
)

echo Starting API Server on port 8080...
echo.

pushd "%ROOT%backend"
java -cp "%OUT%" com.networkmonitor.api.APIServer
popd

endlocal
