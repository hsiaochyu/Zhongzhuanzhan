@echo off
chcp 65001 >nul
set JAVA_HOME=D:\26356\WorkBuddy\Workspace\.toolchain\jdk\jdk-21.0.13+9
set GRADLE_USER_HOME=D:\26356\WorkBuddy\Workspace\.toolchain\gradle-home
set PATH=%JAVA_HOME%\bin;%PATH%
call D:\26356\WorkBuddy\Workspace\.toolchain\gradle-8.9\bin\gradle.bat assembleDebug --console=plain
if errorlevel 1 (
  echo.
  echo BUILD FAILED
  exit /b 1
)
echo.
echo BUILD OK -^> app\build\outputs\apk\debug\app-debug.apk
