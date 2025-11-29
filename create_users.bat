@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM Настройка хоста и порта
set HOST_PORT=localhost:8080

echo Creating users on %HOST_PORT%...
echo.

REM Функция для создания пользователя
:create_user
set username=%~1
set name=%~2
set surname=%~3
set role=%~4
set password=%~5

REM Создаем временный JSON файл через PowerShell для правильной обработки кириллицы
set json_file=%TEMP%\create_user_%RANDOM%.json

if "!surname!"=="" (
    powershell -Command "$body = @{username='!username!'; password='!password!'; name='!name!'; role='!role!'}; $body | ConvertTo-Json -Compress | Out-File -FilePath '!json_file!' -Encoding UTF8 -NoNewline"
) else (
    powershell -Command "$body = @{username='!username!'; password='!password!'; name='!name!'; surname='!surname!'; role='!role!'}; $body | ConvertTo-Json -Compress | Out-File -FilePath '!json_file!' -Encoding UTF8 -NoNewline"
)

echo Creating user: !username! (!name! !surname!) with role: !role!, password: !password!

curl -X POST "http://!HOST_PORT!/api/v1/auth/register" ^
    -H "Content-Type: application/json" ^
    -d "@!json_file!" ^
    -s -o nul

if errorlevel 1 (
    echo Error creating user !username!
) else (
    echo User created successfully: !username!
)

del "!json_file!" >nul 2>&1
echo ---
echo.
goto :eof

REM Основные пользователи
call :create_user "lena" "Елена" "Васильева" "SUPERADMIN" "123456"
call :create_user "dima" "Дмитрий" "Пыльцов" "SUPERADMIN" "123456"
call :create_user "admin" "Админ" "" "SUPERADMIN" "345678"
call :create_user "admin1" "Админ" "" "SUPERADMIN" "456789"
call :create_user "admin2" "Админ" "" "SUPERADMIN" "567890"
call :create_user "admin3" "Админ" "" "SUPERADMIN" "678901"
call :create_user "administrator" "Администратор" "" "ADMINISTRATOR" "789012"
call :create_user "announcer" "Ведущий" "" "ANNOUNCER" "890123"

REM Судьи для лидеров
call :create_user "bogdan" "Богдан" "Болдырев" "USER" "901234"
call :create_user "kevin" "Кевин" "Бака-Сегура" "USER" "012345"
call :create_user "egor" "Егор" "Старков" "USER" "123450"
call :create_user "fedor" "Федор" "Гришин" "USER" "234561"
call :create_user "andrey" "Андрей" "Попов" "USER" "345672"

REM Судьи для фоловеров
call :create_user "darya" "Дарья" "Гаршина" "USER" "456783"
call :create_user "polina" "Полина" "Горбачёва" "USER" "567894"
call :create_user "yana" "Яна" "Ишмаева" "USER" "678905"
call :create_user "mila" "Мила" "Пономаренко" "USER" "789016"
call :create_user "masha" "Мария" "Болдырева" "USER" "890127"

echo.
echo All users created!
pause
