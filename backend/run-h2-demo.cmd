@echo off
setlocal
cd /d "%~dp0"
call "C:\Users\Mousumi Akter\.m2\wrapper\dists\apache-maven-3.9.14\ed7edd442f634ac1c1ef5ba2b61b6d690b5221091f1a8e1123f5fadcc967520d\bin\mvn.cmd" --batch-mode --no-transfer-progress -Dspring-boot.run.profiles=h2demo spring-boot:run
