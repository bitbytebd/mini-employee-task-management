@echo off
setlocal
cd /d "%~dp0"
set "NODE_EXE=C:\Users\Mousumi Akter\AppData\Local\FIFO-Tech\runtimes\node-v16.20.2-win-x64\node.exe"
"%NODE_EXE%" ".\node_modules\@angular\cli\bin\ng.js" serve --host 127.0.0.1 --port 4200 --configuration development --live-reload=false
