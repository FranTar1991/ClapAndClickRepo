@echo off
rem Set default values
set default_ip_address=192.168.88.239

rem Prompt for IP address, using the default if none is entered
set /p ip_address=Enter IP address [%default_ip_address%]: 
if "%ip_address%"=="" set ip_address=%default_ip_address%

set /p port_number=Enter port number:

cd /d "C:\Users\user\AppData\Local\Android\Sdk\platform-tools"

adb connect %ip_address%:%port_number%