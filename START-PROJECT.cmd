@echo off
setlocal EnableExtensions
set "ROOT=%~dp0"
set "BACKEND_PID=%ROOT%.project-backend.pid"
set "FRONTEND_PID=%ROOT%.project-frontend.pid"

call :port_open 8080
if errorlevel 1 (
    echo Starting FIFO backend demo in a separate window...
    for /f %%P in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "$bytes=New-Object byte[] 48; [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes); $secret=[Convert]::ToBase64String($bytes); $argument='/k title FIFO Backend Demo & set JWT_SECRET='+$secret+' & call %ROOT%backend\run-h2-demo.cmd'; (Start-Process -FilePath cmd.exe -WorkingDirectory '%ROOT%backend' -ArgumentList $argument -PassThru).Id"') do > "%BACKEND_PID%" echo %%P
) else (
    echo Backend is already listening on port 8080.
)

call :port_open 4200
if errorlevel 1 (
    echo Starting FIFO frontend in a separate window...
    for /f %%P in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "$argument='/k title FIFO Frontend Demo & call %ROOT%frontend\run-dev-server.cmd'; (Start-Process -FilePath cmd.exe -WorkingDirectory '%ROOT%frontend' -ArgumentList $argument -PassThru).Id"') do > "%FRONTEND_PID%" echo %%P
) else (
    echo Frontend is already listening on port 4200.
)

echo Waiting for the frontend at http://localhost:4200/login ...
for /l %%I in (1,1,36) do (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "try { $response=Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:4200' -TimeoutSec 3; if($response.StatusCode -eq 200){exit 0} } catch {}; exit 1"
    if not errorlevel 1 goto :open_browser
    ping 127.0.0.1 -n 6 >nul
)

echo.
echo ERROR: The frontend did not respond at http://localhost:4200/login within 3 minutes.
echo Check the FIFO Frontend Demo window for the first startup error.
exit /b 1

:open_browser
echo Frontend is ready. Opening the login page...
start "" "http://localhost:4200/login"
exit /b 0

:port_open
powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Get-NetTCPConnection -State Listen -LocalPort %~1 -ErrorAction SilentlyContinue) { exit 0 } else { exit 1 }"
exit /b %errorlevel%
