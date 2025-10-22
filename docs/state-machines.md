# State Machines

## RoundStateMachine

- Начальное состояние: `DRAFT`
- Список состояний: `DRAFT`, `PLANNED`, `IN_PROGRESS`, `READY`, `COMPLETED`
- Список событий: `DRAFT`, `PLAN`, `START`, `MARK_READY`, `COMPLETE`

### Переходы

| От          | Событие    | Куда        | Назначение                                                                                  |
|-------------|------------|-------------|---------------------------------------------------------------------------------------------|
| DRAFT       | PLAN       | PLANNED     | Раунд просмотрен/отредактирован админом, готов к старту. Можно добавлять/удалять участников |
| PLANNED     | DRAFT      | DRAFT       | Нужно отредактировать участников                                                            |
| PLANNED     | START      | IN_PROGRESS | Старт раунда; статусы судей в `NOT_READY`, прежние результаты очищаются                     |
| IN_PROGRESS | DRAFT      | DRAFT       | Требуется оперативное редактирование состава участников                                     |
| IN_PROGRESS | MARK_READY | READY       | Все судьи выставили оценки, раунд готов к завершению                                        |
| READY       | START      | IN_PROGRESS | Кто-то откатил статус на `NOT_READY`, продолжаем раунд                                      |
| READY       | COMPLETE   | COMPLETED   | Окончательные результаты готовы                                                             |
| COMPLETED   | START      | IN_PROGRESS | (Возможно не нужен) Возврат к работе над раундом                                            |
| IN_PROGRESS | PLAN       | PLANNED     | (Возможно не нужен) Планирование заново                                                     |

## MilestoneStateMachine

- Начальное состояние: `DRAFT`
- Список состояний: `DRAFT`, `PLANNED`, `PENDING`, `IN_PROGRESS`, `SUMMARIZING`, `COMPLETED`
- Список событий: `DRAFT`, `PLAN`, `PREPARE_ROUNDS`, `START`, `SUM_UP`, `COMPLETE`

### Переходы

| От          | Событие        | Куда        | Назначение                                                                     |
|-------------|----------------|-------------|--------------------------------------------------------------------------------|
| DRAFT       | PLAN           | PLANNED     | Настроены правила; создаются раунды в `DRAFT` с авто-распределением            |
| PLANNED     | DRAFT          | DRAFT       | Изменение правил                                                               |
| PLANNED     | PREPARE_ROUNDS | PENDING     | Распределение участников по раундам и проверка админом                         |
| PENDING     | START          | IN_PROGRESS | Этап стартует, раунды сформированы, допустимо редактирование при необходимости |
| IN_PROGRESS | SUM_UP         | SUMMARIZING | Все раунды завершены (`READY`), предварительный подсчет                        |
| SUMMARIZING | START          | IN_PROGRESS | Нужен дополнительный раунд — продолжаем этап                                   |
| SUMMARIZING | COMPLETE       | COMPLETED   | Финализация этапа                                                              |
| PENDING     | PLAN           | PLANNED     | (Возможно не нужен) Возврат к планированию                                     |
| IN_PROGRESS | PREPARE_ROUNDS | PENDING     | (Возможно не нужен) Возврат к подготовке раундов                               |
