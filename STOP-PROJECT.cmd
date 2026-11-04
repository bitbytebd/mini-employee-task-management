@echo off
setlocal EnableExtensions
set "ROOT=%~dp0"
call :stop_owned "%ROOT%.project-backend.pid" "FIFO Backend Demo" "run-h2-demo.cmd"
call :stop_owned "%ROOT%.project-frontend.pid" "FIFO Frontend Demo" "run-dev-server.cmd"
exit /b 0

:stop_owned
set "PID_FILE=%~1"
set "TITLE=%~2"
set "MARKER=%~3"
if not exist "%PID_FILE%" (
    echo No tracked %TITLE% launcher process.
    exit /b 0
)
set /p PID=<"%PID_FILE%"
if not defined PID (
    del "%PID_FILE%" >nul 2>&1
    exit /b 0
)
set "PROJECT_PID=%PID%"
set "PROJECT_MARKER=%MARKER%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$p=Get-CimInstance Win32_Process -Filter ('ProcessId='+$env:PROJECT_PID) -ErrorAction SilentlyContinue; if($p -and $p.CommandLine -match [regex]::Escape($env:PROJECT_MARKER)){exit 0}; exit 1" 2>nul
if errorlevel 1 (
    echo Refusing to stop PID %PID%: it is not a tracked %TITLE% process.
    del "%PID_FILE%" >nul 2>&1
    exit /b 0
)
echo Stopping tracked %TITLE% process tree (PID %PID%)...
taskkill /pid %PID% /t >nul 2>&1
del "%PID_FILE%" >nul 2>&1
exit /b 0
