@echo off
if not exist out mkdir out
dir /s /b src\*.java | findstr /v /i "mysql-connector" > sources.txt
javac -d out -cp "lib\*" @sources.txt
if %errorlevel% neq 0 (echo COMPILE FAILED) else (echo COMPILE OK)
del sources.txt