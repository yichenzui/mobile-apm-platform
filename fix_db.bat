@echo off
chcp 65001 >nul
echo ==========================================
echo   数据库修复脚本
echo ==========================================
echo.

mysql -u root -p123456789 mobile_apm < "%~dp0backend\src\main\resources\db\fix_db.sql" 2>&1

echo.
if %errorlevel% equ 0 (
    echo [SUCCESS] 数据库修复成功！
) else (
    echo [ERROR] 数据库修复失败，请检查MySQL是否启动
)
pause
