### 🔹 Сборка и пуш в GitHub Container Registry (GHCR)

### Для чего нужен этот образ
- **Назначение**: образ публикуется в `ghcr.io` и далее используется в репозитории `belkanaphan-sensation/infra`. Flux в кластере автоматически подхватит новую версию образа и перезапустит приложение с обновлённым тегом (при настроенной Image Automation/HelmRelease/Kustomization).

### Версионирование образа при сборке
- **Схема тега**: `<APP_VERSION>-<DDMMYYYY>.<BUILD_NUMBER>`
  - `APP_VERSION` — базовая версия из `build.gradle` (поле `version`).
  - `DDMMYYYY` — дата сборки.
  - `BUILD_NUMBER` — порядковый номер/номер запуска CI (по умолчанию `0`).

Пример: ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-31082025.0

### Сборка и публикация
1. Создай **Personal Access Token** с правами `write:packages` и `read:packages` (см. ниже).
2. Выполни команды:

```bash
# Логин в GHCR
echo $CR_PAT | docker login ghcr.io -u belkanaphan --password-stdin

# Сборка Docker-образа под linux/amd64
docker build --platform linux/amd64 -t ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-31082025.0 .

# Публикация образа
docker push ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-31082025.0
```

- `$CR_PAT` — переменная окружения с токеном (см. раздел ниже).

### Как получить токен GitHub (для GHCR)
1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic) → Generate new token (classic).
2. Отметь скоупы: `write:packages`, `read:packages` (опционально `delete:packages`). Если пуш идёт от CI к приватному пакету, может понадобиться `repo`.
3. Выбери срок действия и создай токен. Скопируй значение.
4. Если организация использует SSO — разреши доступ токена к организации (Enable SSO).
5. Экспортируй в терминале: `export CR_PAT='<ВАШ_ТОКЕН>'`.

## Для работы с ghcr.io можно использовать утилиту [gh](https://github.com/cli/cli).

### Список всех пакетов контейнеров организации
gh api orgs/belkanaphan-sensation/packages?package_type=container

### Список версий конкретного пакета
gh api "orgs/belkanaphan-sensation/packages/container/tournament-ss/versions"

### Удаление версии по id
gh api --method DELETE "orgs/belkanaphan-sensation/packages/container/tournament-ss/versions/<VERSION_ID>"