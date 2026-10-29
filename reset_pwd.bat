@echo off
chcp 65001 >nul
echo Resetting admin password...
mysql -u root -p123456789 mobile_apm -e "UPDATE user SET password='$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' WHERE username='admin';" 2>nul
if %errorlevel% equ 0 (
    echo [OK] Admin password has been reset!
    echo.
    echo New login credentials:
    echo   Username: admin
    echo   Password: 123456
) else (
    echo [ERROR] Failed to reset password
)
pause
