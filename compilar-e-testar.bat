@echo off
setlocal
set "MAVEN=%~dp0.tools\apache-maven-3.9.11\bin\mvn.cmd"
if not exist "%MAVEN%" (
  set "MAVEN=mvn.cmd"
)
call "%MAVEN%" -Dmaven.repo.local="%~dp0.m2repo" test
