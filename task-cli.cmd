@echo off
chcp 65001 >nul
setlocal

set "SCRIPT_DIR=%~dp0"
set "JAR=%SCRIPT_DIR%target\task-cli.jar"

if not exist "%JAR%" (
    echo Building task-cli...
    call mvn -q package -f "%SCRIPT_DIR%pom.xml"
)

java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar "%JAR%" %*