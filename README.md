### 🔹 Сборка и пуш в GitHub Container Registry (GHCR)

> Отсюда кубер будет забирать образ

1. Создай **Personal Access Token** в GitHub с правами `write:packages` и `read:packages`.
2. Локально в терминале:

```bash
# Сборка Docker-образа
docker build -t ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-SNAPSHOT .

# Логин в GHCR
echo $CR_PAT | docker login ghcr.io -u belkanaphan --password-stdin

# Пуш образа в GHCR
docker push ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-SNAPSHOT
```

* `$CR_PAT` — переменная окружения с токеном.
