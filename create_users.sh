#!/bin/bash

# Настройка хоста и порта
HOST_PORT="localhost:8080"

# Функция для создания пользователя
create_user() {
    local username=$1
    local name=$2
    local surname=$3
    local role=$4
    local password=$5
    
    # Формируем JSON с учетом пустых значений
    local json_data
    if [ -z "$surname" ]; then
        json_data="{
            \"username\": \"$username\",
            \"password\": \"$password\",
            \"name\": \"$name\",
            \"role\": \"$role\"
        }"
    else
        json_data="{
            \"username\": \"$username\",
            \"password\": \"$password\",
            \"name\": \"$name\",
            \"surname\": \"$surname\",
            \"role\": \"$role\"
        }"
    fi
    
    echo "Creating user: $username ($name ${surname:-''}) with role: $role, password: $password"
    
    curl -X POST "http://${HOST_PORT}/api/v1/auth/register" \
        -H "Content-Type: application/json" \
        -d "$json_data"
    
    echo ""
    echo "---"
}

# Создание пользователей
echo "Creating users on ${HOST_PORT}..."
echo ""

# Основные пользователи
create_user "lena" "Елена" "Васильева" "SUPERADMIN" "123456"
create_user "dima" "Дмитрий" "Пыльцов" "SUPERADMIN" "123456"
create_user "admin" "Админ" "" "SUPERADMIN" "345678"
create_user "admin1" "Админ" "" "SUPERADMIN" "456789"
create_user "admin2" "Админ" "" "SUPERADMIN" "567890"
create_user "admin3" "Админ" "" "SUPERADMIN" "678901"
create_user "administrator" "Администратор" "" "ADMINISTRATOR" "789012"
create_user "announcer" "Ведущий" "" "ANNOUNCER" "890123"

# Судьи для лидеров
create_user "bogdan" "Богдан" "Болдырев" "USER" "901234"
create_user "kevin" "Кевин" "Бака-Сегура" "USER" "012345"
create_user "egor" "Егор" "Старков" "USER" "123450"
create_user "fedor" "Федор" "Гришин" "USER" "234561"
create_user "andrey" "Андрей" "Попов" "USER" "345672"

# Судьи для фоловеров
create_user "darya" "Дарья" "Гаршина" "USER" "456783"
create_user "polina" "Полина" "Горбачёва" "USER" "567894"
create_user "yana" "Яна" "Ишмаева" "USER" "678905"
create_user "mila" "Мила" "Пономаренко" "USER" "789016"
create_user "masha" "Мария" "Болдырева" "USER" "890127"

echo ""
echo "All users created!"

