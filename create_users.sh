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
create_user "lena" "Елена" "Васильева" "SUPERADMIN" "906090"
create_user "dima" "Дмитрий" "Пыльцов" "SUPERADMIN" "906090"
create_user "admin" "Админ" "" "SUPERADMIN" "906090"
create_user "admin1" "Админ" "" "SUPERADMIN" "906090"
create_user "admin2" "Админ" "" "SUPERADMIN" "906090"
create_user "admin3" "Админ" "" "SUPERADMIN" "906090"
create_user "administrator" "Администратор" "" "ADMINISTRATOR" "123456"
create_user "administrator1" "Администратор" "" "ADMINISTRATOR" "123456"
create_user "announcer" "Ведущий" "" "ANNOUNCER" "123456"
create_user "masha_a" "Ведущая" "Маша" "ANNOUNCER" "123456"
create_user "bogdan_a" "Ведущий" "Богдан" "ANNOUNCER" "123456"

# Судьи для лидеров
create_user "bogdan" "Богдан" "Болдырев" "USER" "901234"
create_user "kevin" "Кевин" "Бака-Сегура" "USER" "012345"
create_user "egor" "Егор" "Старков" "USER" "123450"
create_user "fedor" "Федор" "Гришин" "USER" "234561"
create_user "andrey" "Андрей" "Попов" "USER" "345672"
create_user "ruslan" "Руслан" "Сагинбаев" "USER" "456789"
create_user "firgat" "Фиргат" "Валеев" "USER" "567890"
create_user "andres" "Андрес" "Барбоса" "USER" "678901"
create_user "maksim" "Максим" "Лаврин" "USER" "789012"
create_user "ayrat" "Айрат" "Хазиев" "USER" "567890"
create_user "daviel" "Давиэль" "Айяла" "USER" "678901"
create_user "kirill" "Кирилл" "Адаричев" "USER" "890123"
create_user "ildar" "Ильдар" "Вагапов" "USER" "901234"
create_user "giorgi" "Гиорги" "Коберидзе" "USER" "012345"
create_user "bolat" "Болат" "Айтпай" "USER" "123456"
create_user "ivan" "Иван" "Маренинов" "USER" "234567"
create_user "kanat" "Канат" "Дутбаев" "USER" "345678"
create_user "jhersy" "Джерси" "" "USER" "456789"
create_user "yakov" "Яков" "Целищев" "USER" "567890"
create_user "daniel" "Даниэль" "Торриенте" "USER" "678901"
create_user "german" "Герман" "Констанц" "USER" "789012"
create_user "gabriel" "Габриэль" "Аяла" "USER" "890123"
create_user "evgeny" "Евгений" "Куликов" "USER" "890123"

# Судьи для фоловеров
create_user "darya" "Дарья" "Гаршина" "USER" "890123"
create_user "polina" "Полина" "Горбачёва" "USER" "901245"
create_user "yana" "Яна" "Ишмаева" "USER" "012356"
create_user "mila" "Мила" "Пономаренко" "USER" "123467"
create_user "masha" "Мария" "Болдырева" "USER" "234578"
create_user "albina" "Альбина" "Ахметгалиева" "USER" "345689"
create_user "valery" "Валерия" "Чехова" "USER" "456790"
create_user "mariya" "Мария" "Никитина" "USER" "567801"
create_user "adelina" "Аделина" "Гумирова" "USER" "678912"
create_user "elvira" "Эльвира" "Акбашева" "USER" "789023"
create_user "natalya" "Наталья" "Чеботарь" "USER" "123467"
create_user "polina_k" "Полина" "Клейменова" "USER" "567801"
create_user "irina" "Ирина" "Чибаева" "USER" "789023"
create_user "kristina" "Кристина" "Вагапова" "USER" "890134"
create_user "ekaterina" "Екатерина" "Гальцова" "USER" "901245"
create_user "evgenia" "Евгения" "Крылова" "USER" "012356"
create_user "yulia" "Юлия" "Иванская" "USER" "234578"
create_user "elizaveta" "Елизавета" "Медведева" "USER" "345689"
create_user "darya_g" "Дарья" "Гагина" "USER" "456790"

echo ""
echo "All users created!"

