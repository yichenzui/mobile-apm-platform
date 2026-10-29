@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
echo Starting Backend...

set "ROOT=%~dp0"

:: 加载 .env 中的敏感配置（数据库密码 / JWT 密钥 已从 yml 移除）
if exist "%ROOT%.env" (
    for /f "usebackq tokens=1,* delims==" %%a in ("%ROOT%.env") do (
        if not "%%a"=="" (
            set "k=%%a"
            if not "!k:~0,1!"=="#" set "%%a=%%b"
        )
    )
    echo [env] 已加载 .env
) else (
    echo [env][警告] 未找到 .env，后端可能因缺少 DB_USERNAME/DB_PASSWORD/JWT_SECRET 启动失败
)

cd /d "%ROOT%backend"
call mvn spring-boot:run 2>&1
