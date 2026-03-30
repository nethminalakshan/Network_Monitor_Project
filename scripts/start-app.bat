@echo off
setlocal

for %%i in ("%~dp0..") do set "ROOT=%%~fi\"
set "OUT=%ROOT%backend\out"

REM Master script to start both backend and frontend

echo ╔═══════════════════════════════════════════════════════════╗
echo ║                                                           ║
echo ║            # NETWORK MONITOR - WEB APPLICATION #          ║
echo ║                                                           ║
echo ║              Starting Full Stack Application              ║
echo ║                                                           ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

REM Check if Node.js is installed
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Node.js is not installed!
    echo Please install Node.js from: https://nodejs.org/
    echo.
    pause
    endlocal
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Java is not installed!
    echo Please install Java JDK 8 or higher
    echo.
    pause
    endlocal
    exit /b 1
)

echo ✅ Prerequisites check passed
echo.

REM Compile backend if needed
if not exist "%OUT%\com\networkmonitor\api\APIServer.class" (
    echo 🔨 Compiling backend...
    call "%ROOT%compile-backend.bat"
    if %errorlevel% neq 0 (
        echo ❌ Backend compilation failed!
        pause
        endlocal
        exit /b 1
    )
)

REM Install frontend dependencies if needed
if not exist "%ROOT%frontend\node_modules" (
    echo 📦 Installing frontend dependencies...
    pushd "%ROOT%frontend"
    call npm install
    if %errorlevel% neq 0 (
        echo ❌ Frontend setup failed!
        pause
        popd
        endlocal
        exit /b 1
    )
    popd
)

echo.
echo ========================================
echo   🚀 Starting Applications
echo ========================================
echo.
echo Backend API: http://localhost:8080
echo Frontend UI: http://localhost:3000
echo.
echo ⚠️  Two terminal windows will open:
echo    1. Backend API Server (port 8080)
echo    2. React Frontend (port 3000)
echo.
echo 💡 Both need to run simultaneously!
echo 🛑 Close both windows to stop the application
echo.

pause

REM Start backend in new window
echo Starting Backend API Server...
start "Network Monitor - Backend API" /d "%~dp0" cmd /k start-backend.bat

REM Wait a bit for backend to start
timeout /t 3 /nobreak >nul

echo Starting Frontend UI...
start "Network Monitor - Frontend UI" /d "%~dp0" cmd /k start-frontend.bat

echo.
echo ✅ Application started successfully!
echo.
echo 📖 Open your browser and navigate to:
echo    http://localhost:3000
echo.

endlocal
