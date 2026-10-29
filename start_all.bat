@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title APM 一键启动

set "ROOT=%~dp0"
:: 本机 Redis 便携版路径（无密码，与 application.yml 的 spring.data.redis 配置一致）
set "REDIS_EXE=D:\learn\ruan jian\redis-server.exe"

:: 加载 .env 中的敏感配置（数据库密码 / JWT 密钥 / AI Key 已从 yml 移除）
if exist "%ROOT%.env" (
    for /f "usebackq tokens=1,* delims==" %%a in ("%ROOT%.env") do (
        if not "%%a"=="" (
            set "k=%%a"
            if not "!k:~0,1!"=="#" set "%%a=%%b"
        )
    )
    echo [env] 已加载 .env
) else (
    echo [env][警告] 未找到 .env，请复制 .env.example 为 .env 并填写后重试
)

echo ============================================
echo    APM 性能监控平台 - 一键启动
echo ============================================
echo.

:: ==================== 1. MySQL ====================
echo [1/4] 检查 MySQL (3306)...
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] MySQL 已在运行
) else (
    echo   未检测到 MySQL，尝试启动 MySQL80 服务...
    net start MySQL80 >nul 2>&1
    ping -n 5 127.0.0.1 >nul
    netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
    if !errorlevel! equ 0 (
        echo   [OK] MySQL 已启动
    ) else (
        echo   [警告] MySQL 未就绪，后端将连不上数据库，请手动启动
    )
)

:: ==================== 2. Redis ====================
echo.
echo [2/4] 检查 Redis (6379)...
netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] Redis 已在运行
) else (
    echo   未检测到 Redis，尝试启动（无密码模式）...
    if exist "%REDIS_EXE%" (
        start "APM-Redis" /min "" "%REDIS_EXE%"
        ping -n 4 127.0.0.1 >nul
    ) else (
        where redis-server >nul 2>&1
        if !errorlevel! equ 0 (
            start "APM-Redis" /min "" redis-server
            ping -n 4 127.0.0.1 >nul
        ) else (
            echo   [提示] 未找到 redis-server，将以无缓存模式运行（不影响核心功能）
        )
    )
    netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>&1
    if !errorlevel! equ 0 (
        echo   [OK] Redis 已启动
    ) else (
        echo   [提示] Redis 未启动，核心功能仍可用（在线用户数会显示为 0）
    )
)

:: ==================== 3. 后端 ====================
echo.
echo [3/4] 启动后端 Spring Boot (8080)...
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] 后端已在运行，跳过
) else (
    where mvn >nul 2>&1
    if !errorlevel! neq 0 (
        echo   [错误] 未找到 mvn 命令，请确认 Maven 已安装并加入 PATH
        goto :frontend
    )
    start "APM-Backend" /d "%ROOT%backend" cmd /c "mvn spring-boot:run > ..\backend.log 2>&1"
    echo   后端启动中（首次约需 30-60 秒）...
    for /l %%i in (1,1,25) do (
        ping -n 4 127.0.0.1 >nul
        netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
        if !errorlevel! equ 0 (
            echo   [OK] 后端已就绪
            goto :frontend
        )
    )
    echo   [警告] 后端启动超时，请查看 backend.log
)

:: ==================== 4. 前端 ====================
:frontend
echo.
echo [4/4] 启动前端 Vite Dev Server (3000)...
netstat -ano | findstr ":3000" | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo   [OK] 前端已在运行，跳过
) else (
    if not exist "%ROOT%frontend\node_modules" (
        echo   首次运行，正在安装前端依赖，请耐心等待...
        cd /d "%ROOT%frontend"
        call npm install
        cd /d "%ROOT%"
    )
    start "APM-Frontend" /d "%ROOT%frontend" cmd /c "npm run dev > ..\frontend.log 2>&1"
    echo   前端启动中...
    for /l %%i in (1,1,15) do (
        ping -n 3 127.0.0.1 >nul
        netstat -ano | findstr ":3000" | findstr "LISTENING" >nul 2>&1
        if !errorlevel! equ 0 (
            echo   [OK] 前端已就绪
            goto :done
        )
    )
    echo   [警告] 前端启动超时，请查看 frontend.log
)

:done
echo.
echo ============================================
echo    启动完成
echo ============================================
echo    前端地址: http://localhost:3000
echo    后端地址: http://localhost:8080/api
echo    接口文档: http://localhost:8080/api/swagger-ui.html 或 http://localhost:8080/api/doc.html
echo    登录账号: admin
echo    登录密码: 123456789
echo ============================================
echo.
echo 提示:
echo   - 服务运行在独立窗口（APM-Backend / APM-Frontend），关闭对应窗口即可停止
echo   - 日志: backend.log / frontend.log
echo   - 如需重启后端: restart_backend.bat
echo.

start http://localhost:3000

pause
