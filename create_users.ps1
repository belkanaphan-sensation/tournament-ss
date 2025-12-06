# Настройка хоста и порта
$HOST_PORT = "localhost:8080"

# Функция для создания пользователя
function Create-User {
    param(
        [string]$username,
        [string]$name,
        [string]$surname,
        [string]$role,
        [string]$password
    )
    
    # Формируем JSON
    $body = @{
        username = $username
        password = $password
        name = $name
        role = $role
    }
    
    if ($surname) {
        $body.surname = $surname
    }
    
    $jsonBody = $body | ConvertTo-Json -Compress
    
    Write-Host "Creating user: $username ($name $surname) with role: $role, password: $password"
    
    try {
        # Конвертируем в байты с явным указанием UTF-8
        $utf8 = [System.Text.Encoding]::UTF8
        $bodyBytes = $utf8.GetBytes($jsonBody)
        
        $response = Invoke-RestMethod -Uri "http://$HOST_PORT/api/v1/auth/register" `
            -Method Post `
            -ContentType "application/json; charset=utf-8" `
            -Body $bodyBytes
        
        Write-Host "User created successfully: $($response.username)" -ForegroundColor Green
    }
    catch {
        Write-Host "Error creating user $username : $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $responseBody = $reader.ReadToEnd()
                Write-Host "Response: $responseBody" -ForegroundColor Yellow
            }
            catch {
                Write-Host "Could not read response body" -ForegroundColor Yellow
            }
        }
    }
    
    Write-Host "---"
}

# Создание пользователей
Write-Host "Creating users on $HOST_PORT..."
Write-Host ""

# Основные пользователи
Create-User -username "lena" -name "Елена" -surname "Васильева" -role "SUPERADMIN" -password "906090"
Create-User -username "dima" -name "Дмитрий" -surname "Пыльцов" -role "SUPERADMIN" -password "906090"
Create-User -username "admin" -name "Админ" -surname "" -role "SUPERADMIN" -password "906090"
Create-User -username "admin1" -name "Админ" -surname "" -role "SUPERADMIN" -password "906090"
Create-User -username "admin2" -name "Админ" -surname "" -role "SUPERADMIN" -password "906090"
Create-User -username "admin3" -name "Админ" -surname "" -role "SUPERADMIN" -password "906090"
Create-User -username "administrator" -name "Администратор" -surname "" -role "ADMINISTRATOR" -password "123456"
Create-User -username "administrator1" -name "Администратор" -surname "" -role "ADMINISTRATOR" -password "123456"
Create-User -username "announcer" -name "Ведущий" -surname "" -role "ANNOUNCER" -password "123456"
Create-User -username "masha_a" -name "Ведущая" -surname "Маша" -role "ANNOUNCER" -password "123456"
Create-User -username "bogdan_a" -name "Ведущий" -surname "Богдан" -role "ANNOUNCER" -password "123456"

# Судьи для лидеров
Create-User -username "bogdan" -name "Богдан" -surname "Болдырев" -role "USER" -password "901234"
Create-User -username "kevin" -name "Кевин" -surname "Бака-Сегура" -role "USER" -password "012345"
Create-User -username "egor" -name "Егор" -surname "Старков" -role "USER" -password "123450"
Create-User -username "fedor" -name "Федор" -surname "Гришин" -role "USER" -password "234561"
Create-User -username "andrey" -name "Андрей" -surname "Попов" -role "USER" -password "345672"
Create-User -username "ruslan" -name "Руслан" -surname "Сагинбаев" -role "USER" -password "456789"
Create-User -username "firgat" -name "Фиргат" -surname "Валеев" -role "USER" -password "567890"
Create-User -username "andres" -name "Андрес" -surname "Барбоса" -role "USER" -password "678901"
Create-User -username "maksim" -name "Максим" -surname "Лаврин" -role "USER" -password "789012"
Create-User -username "ayrat" -name "Айрат" -surname "Хазиев" -role "USER" -password "567890"
Create-User -username "daviel" -name "Давиэль" -surname "Аяла" -role "USER" -password "678901"
Create-User -username "kirill" -name "Кирилл" -surname "Адаричев" -role "USER" -password "890123"
Create-User -username "ildar" -name "Ильдар" -surname "Вагапов" -role "USER" -password "901234"
Create-User -username "giorgi" -name "Гиорги" -surname "Коберидзе" -role "USER" -password "012345"
Create-User -username "bolat" -name "Болат" -surname "Айтпай" -role "USER" -password "123456"
Create-User -username "ivan" -name "Иван" -surname "Маренинов" -role "USER" -password "234567"
Create-User -username "kanat" -name "Канат" -surname "Дутбаев" -role "USER" -password "345678"
Create-User -username "jhersy" -name "Джерси" -surname "" -role "USER" -password "456789"
Create-User -username "yakov" -name "Яков" -surname "Целищев" -role "USER" -password "567890"
Create-User -username "daniel" -name "Даниэль" -surname "Торриенте" -role "USER" -password "678901"
Create-User -username "german" -name "Герман" -surname "Констанц" -role "USER" -password "789012"
Create-User -username "gabriel" -name "Габриэль" -surname "Аяла" -role "USER" -password "890123"

# Судьи для фоловеров
Create-User -username "darya" -name "Дарья" -surname "Гаршина" -role "USER" -password "890123"
Create-User -username "polina" -name "Полина" -surname "Горбачёва" -role "USER" -password "901245"
Create-User -username "yana" -name "Яна" -surname "Ишмаева" -role "USER" -password "012356"
Create-User -username "mila" -name "Мила" -surname "Пономаренко" -role "USER" -password "123467"
Create-User -username "masha" -name "Мария" -surname "Болдырева" -role "USER" -password "234578"
Create-User -username "albina" -name "Альбина" -surname "Ахметгалиева" -role "USER" -password "345689"
Create-User -username "valery" -name "Валерия" -surname "Чехова" -role "USER" -password "456790"
Create-User -username "mariya" -name "Мария" -surname "Никитина" -role "USER" -password "567801"
Create-User -username "adelina" -name "Аделина" -surname "Гумирова" -role "USER" -password "678912"
Create-User -username "elvira" -name "Эльвира" -surname "Акбашева" -role "USER" -password "789023"
Create-User -username "natalya" -name "Наталья" -surname "Чеботарь" -role "USER" -password "123467"
Create-User -username "polina_k" -name "Полина" -surname "Клейменова" -role "USER" -password "567801"
Create-User -username "irina" -name "Ирина" -surname "Чибаева" -role "USER" -password "789023"
Create-User -username "kristina" -name "Кристина" -surname "Вагапова" -role "USER" -password "890134"
Create-User -username "ekaterina" -name "Екатерина" -surname "Гальцова" -role "USER" -password "901245"
Create-User -username "evgenia" -name "Евгения" -surname "Крылова" -role "USER" -password "012356"
Create-User -username "yulia" -name "Юлия" -surname "Иванская" -role "USER" -password "234578"
Create-User -username "elizaveta" -name "Елизавета" -surname "Медведева" -role "USER" -password "345689"
Create-User -username "darya_g" -name "Дарья" -surname "Гагина" -role "USER" -password "456790"

Write-Host ""
Write-Host "All users created!"
