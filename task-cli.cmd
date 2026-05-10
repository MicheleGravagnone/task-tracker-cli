@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "JAR=%SCRIPT_DIR%target\task-cli.jar"

if not exist "%JAR%" (
    echo Building task-cli...
    call mvn -q package -f "%SCRIPT_DIR%pom.xml"
)

java -jar "%JAR%" %*