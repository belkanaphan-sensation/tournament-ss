# Настройки сессии в Spring Boot приложении

## Текущие настройки сессии

### Время жизни сессии
- **Текущее**: 24 часа
- **Конфигурация**: `server.servlet.session.timeout: 24h`
- **Формат**: Поддерживаются единицы времени: `s` (секунды), `m` (минуты), `h` (часы), `d` (дни)

### Настройки cookie сессии
- **Имя cookie**: `JSESSIONID`
- **Максимальный возраст**: 86400 секунд (24 часа)
- **HttpOnly**: `true` (безопасность - cookie недоступен через JavaScript)
- **Secure**: `false` (для разработки, в продакшене должно быть `true`)
- **SameSite**: `lax` (защита от CSRF атак)

### Политика создания сессий
- **Текущая**: `SessionCreationPolicy.IF_REQUIRED` (сессии создаются только при необходимости)
- **Альтернативы**: `ALWAYS`, `NEVER`, `STATELESS`

## Как изменить настройки сессии

### 1. Изменение времени жизни сессии
В файле `application.yml`:
```yaml
server:
  servlet:
    session:
      timeout: 12h  # Изменить на 12 часов
```

### 2. Изменение настроек cookie
```yaml
server:
  servlet:
    session:
      cookie:
        name: MY_SESSION_ID  # Изменить имя cookie
        max-age: 43200       # 12 часов в секундах
        http-only: true      # Безопасность
        secure: true         # Только HTTPS (для продакшена)
        same-site: strict    # Строгая политика SameSite
```

### 3. Программное изменение
В коде можно изменить время жизни сессии:
```java
// В любом контроллере или сервисе
@Autowired
private HttpSession session;

public void extendSession() {
    session.setMaxInactiveInterval(86400); // 24 часа
}
```

## Рекомендации по настройке

### Для разработки
```yaml
server:
  servlet:
    session:
      timeout: 24h
      cookie:
        max-age: 86400
        secure: false
        same-site: lax
```

### Для продакшена
```yaml
server:
  servlet:
    session:
      timeout: 24h  # Длинные сессии для удобства пользователей
      cookie:
        secure: true
        http-only: true
        same-site: strict
        max-age: 86400  # 24 часа
```

### Для высоконагруженных систем
Рекомендуется использовать Spring Session с Redis:
```yaml
spring:
  session:
    store-type: redis
    redis:
      namespace: "tournament:session"
      timeout: 24h
```


## Безопасность

### Рекомендации
1. **Короткие сессии** - уменьшают риск компрометации
2. **HttpOnly cookies** - защита от XSS атак
3. **Secure cookies** - только по HTTPS
4. **SameSite policy** - защита от CSRF
5. **Регулярное обновление** - принудительное обновление сессий

### Очистка сессий
Сессии автоматически очищаются при истечении времени жизни. Для принудительной очистки в коде:
```java
// В любом сервисе или контроллере
@Autowired
private HttpSession session;

public void logout() {
    session.invalidate();
}
```
