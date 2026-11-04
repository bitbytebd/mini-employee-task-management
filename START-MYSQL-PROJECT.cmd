@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -NoExit -File "%~dp0backend\run-mysql-project.ps1"
