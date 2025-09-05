## О проекте

Сервис турниров (tournament-ss) — Spring Boot приложение для управления организациями, мероприятиями, активностями, участниками, раундами, ролями и пользователями. Использует стандартный стек: Spring Data JPA, Liquibase, MapStruct, Lombok, OpenAPI/Swagger.

### Технологии
- **Java 21**, **Gradle**
- **Spring Boot**, **Spring Data JPA**
- **Liquibase** (миграции БД)
- **MapStruct** (маппинг Entity ↔ DTO)
- **Lombok**
- **OpenAPI/Swagger**
- **Docker**, **docker-compose** (инфраструктура)

### Структура кода (упрощённо)
- `src/main/java/org/bn/sensation/`
  - `Application.java` — точка входа
  - `config/` — конфигурации (`JpaConfig`, `JsonConfig`, `SwaggerConfig`)
  - `core/` — доменные модули
    - `common/` — базовые `entity`, `dto`, `mapper`, `service`
    - `organization/`, `occasion/`, `activity/`, `participant/`, `round/`, `role/`, `user/`, `milestone/`
      - `entity/` — JPA сущности
      - `service/dto/` — DTO с описаниями Swagger
      - `service/mapper/` — MapStruct мапперы (`@Mapper(config = BaseDtoMapper.class)`)
      - `presentation/` — REST-контроллеры (где предусмотрены)
- `src/main/resources/db/changelog/` — Liquibase (`db.changelog-master.yaml`, `init/`)

### Требования
- JDK 21+
- Docker и docker-compose (для локальной инфраструктуры)

### Локальный запуск
1. Подними инфраструктуру (например, БД):
```bash
cd docker-compose
./start-infra.sh
```
2. Запусти приложение:
```bash
./gradlew bootRun
```

### Сборка
```bash
./gradlew clean build
```
Собранный JAR: `build/libs/*.jar`.

### Запуск через Docker
```bash
docker build -t tournament-ss:local .
docker run --rm -p 8080:8080 tournament-ss:local
```

### Миграции БД (Liquibase)
- Мастер-файл: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Инициализация: `src/main/resources/db/changelog/init/`
Миграции применяются автоматически при старте.

### OpenAPI/Swagger UI
После запуска приложение публикует спецификацию и UI. Адрес задаётся в `SwaggerConfig` и через SpringDoc (проверь автоконфигурацию). Типичные URL:
- Спецификация: `/v3/api-docs`
- UI: `/swagger-ui/index.html`

### MapStruct и базовая конфигурация
- Общий контракт/конфиг мапперов: `core/common/mapper/BaseDtoMapper.java`
- Аннотация: `@Mapper(config = BaseDtoMapper.class)` на интерфейсах мапперов
- В `BaseDtoMapper` задано: `componentModel = spring`, `unmappedTargetPolicy = IGNORE`

### Полезные Gradle-команды
```bash
./gradlew clean          # очистка
./gradlew build          # сборка
./gradlew bootRun        # запуск
./gradlew test           # тесты
```

### Переменные окружения
Смотри `application.yml`. Для локального запуска часто достаточно значений по умолчанию (profile `default`). Для контейнера переменные можно пробрасывать через Docker.

### Стандарты DTO/Mapper
- DTO расположены в `core/<domain>/service/dto/` и содержат `@Schema` описания
- Мапперы в `core/<domain>/service/mapper/` расширяют `BaseDtoMapper<E, D>` и помечены `@Mapper(config = BaseDtoMapper.class)`
