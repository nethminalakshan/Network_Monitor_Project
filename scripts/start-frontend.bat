@echo off
setlocal
REM Setup and Start React Frontend

for %%i in ("%~dp0..") do set "ROOT=%%~fi\"

echo ========================================
echo   Network Monitor Frontend Setup
echo ========================================
echo.

pushd "%ROOT%frontend"

REM Check if node_modules exists
if not exist "node_modules" (
    echo 📦 Installing dependencies...
    echo This may take a few minutes...
    echo.
    call npm install
    
    if %errorlevel% neq 0 (
        echo.
        echo ❌ Failed to install dependencies!
        echo Make sure Node.js and npm are installed.
        echo Download from: https://nodejs.org/
        popd
        endlocal
        exit /b 1
    )
)

echo.
echo ========================================
echo   Starting React Development Server
echo ========================================
echo.
echo Frontend will be available at: http://localhost:3000
echo (Browser will auto-open on first start)
echo.

call npm run dev -- --open

popd
endlocal
