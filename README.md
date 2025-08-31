### 🔹 Сборка и пуш в GitHub Container Registry (GHCR)

> Отсюда кубер будет забирать образ

1. Создай **Personal Access Token** в GitHub с правами `write:packages` и `read:packages`.
2. Локально в терминале:

```bash
# Сборка Docker-образа
docker build --platform linux/amd64 -t ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-SNAPSHOT .

# Логин в GHCR
echo $CR_PAT | docker login ghcr.io -u belkanaphan --password-stdin

# Пуш образа в GHCR
docker push ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-SNAPSHOT
```

* `$CR_PAT` — переменная окружения с токеном.

## Для работы с ghcr.io можно использовать утилиту [gh](https://github.com/cli/cli).

### Список всех пакетов контейнеров организации
gh api orgs/belkanaphan-sensation/packages?package_type=container

### Список версий конкретного пакета
gh api "orgs/belkanaphan-sensation/packages/container/tournament-ss/versions"

### Удаление версии по id
gh api --method DELETE "orgs/belkanaphan-sensation/packages/container/tournament-ss/versions/<VERSION_ID>"