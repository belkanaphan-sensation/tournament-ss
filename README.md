### 🔹 Сборка и пуш в GitHub Container Registry (GHCR)

1. Создай **Personal Access Token** в GitHub с правами `write:packages` и `read:packages`.
2. Локально в терминале:

```bash
# Сборка Docker-образа
docker build -t ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-SNAPSHOT .

# Логин в GHCR
echo $CR_PAT | docker login ghcr.io -u <github-username-or-org> --password-stdin

# Пуш образа в GHCR
docker push ghcr.io/belkanaphan-sensation/tournament-ss:0.0.1-SNAPSHOT
```

* `$CR_PAT` — переменная окружения с токеном.
* `<github-username-or-org>` — твоя организация `belkanaphan-sensation`.
