@echo off
setlocal
set "MAVEN=%~dp0.tools\apache-maven-3.9.11\bin\mvn.cmd"
if not exist "%MAVEN%" set "MAVEN=mvn.cmd"
set "CLASSES=%~dp0target\classes"
set "H2=%~dp0.m2repo\com\h2database\h2\2.3.232\h2-2.3.232.jar"
if not exist "%CLASSES%\br\edu\unicesumar\chat\client\ClienteChat.class" (
  call "%MAVEN%" -Dmaven.repo.local="%~dp0.m2repo" compile
  if errorlevel 1 exit /b 1
)
if not exist "%H2%" (
  call "%MAVEN%" -Dmaven.repo.local="%~dp0.m2repo" dependency:resolve
  if errorlevel 1 exit /b 1
)
java -cp "%CLASSES%;%H2%" br.edu.unicesumar.chat.client.ClienteChat
