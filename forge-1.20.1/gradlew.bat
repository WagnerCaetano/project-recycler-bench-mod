@echo off
setlocal
set "HERE=%~dp0"
call "%HERE%..\gradlew.bat" -p "%HERE%." %*
exit /b %ERRORLEVEL%
