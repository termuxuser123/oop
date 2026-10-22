@echo off
if not exist out mkdir out
powershell -NoProfile -Command "Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { '\"' + ($_.FullName -replace '\\', '/') + '\"' } | Set-Content -Encoding ascii sources.txt"
javac -d out -cp "lib\*" @sources.txt
if %errorlevel% neq 0 (echo COMPILE FAILED) else (echo COMPILE OK)
del sources.txt
