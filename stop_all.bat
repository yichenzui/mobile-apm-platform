@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title APM 一键停止

echo ============================================
echo    APM 性能监控平台 - 停止服务
echo ============================================
echo.

:: 按端口精确终止，避免误杀 IDE 或其他 java/node 进程
call :killByPort 8080 "后端 Spring Boot"
call :killByPort 3000 "前端 Vite"

echo.
echo ============================================
echo    已停止
echo ============================================
echo 说明: Redis / MySQL 是系统服务，本脚本不会停止它们。
echo.
pause
exit /b 0

:killByPort
set "PORT=%~1"
set "NAME=%~2"
set "KILLED=0"
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
    if "%%p" neq "0" (
        taskkill /PID %%p /F >nul 2>&1
        if !errorlevel! equ 0 (
            echo   [OK] 已停止 %NAME% (端口 %PORT%, PID %%p)
            set "KILLED=1"
        )
    )
)
if "%KILLED%"=="0" echo   [提示] %NAME% (端口 %PORT%) 未在运行
exit /b 0
