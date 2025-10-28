Создаем мероприятие Occasion (доступные состояния DRAFT, PLANNED, IN_PROGRESS, COMPLETED)

В нем создаем активность Activity (доступные состояния DRAFT, PLANNED, REGISTRATION_CLOSED, IN_PROGRESS, COMPLETED)
(допустимые состояния мероприятия для создания активности: DRAFT, PLANNED, IN_PROGRESS, недопустимые: COMPLETED)

В ней создаем этапы с заданным вручную порядком Milestone (доступные состояния DRAFT, PLANNED, PENDING, IN_PROGRESS,
SUMMARIZING, COMPLETED)
(допустимые состояния активности для создания этапа: DRAFT)

Создаем правила для этапов. MilestoneRule. (допустимые состояния этапа: DRAFT).
Этап не может быть переведен в PLANNED если у него отсутствует MilestoneRule или Activity в DRAFT.
Активность не может быть переведена в PLANNED если у какого то из этапов отсутствует MilestoneRule.
Если активность уже не в DRAFT и нужно добавить новый этап, то при создании этапа она переходит в DRAFT автоматически.
Т.е. нужна следующая последовательность
активность(DRAFT)-создаем этапы(DRAFT), правила этапа-активность(PLANNED)

Создаем участников активности. Participant ParticipantController.create
Регистрируем участников с присваиванием номера. ParticipantController.update
Когда регистратор зарегистрировал всех участников (как ему кажется) то активность(REGISTRATION_CLOSED)
Зарегистрированные (отмеченные) участники привязываются к этапу и из них формируются раунды. Участники также привязаны к
раундам. RoundController.generateRoundsWithParticipants
Тут создались этап(PLANNED), раунды (DRAFT) формируются JudgeRoundStatus (NOT_READY), JudgeMilestoneStatus (NOT_READY)
Участник может быть отвязан от раунда и привязан к другому раунду этапа к которому он тоже должен быть привязан. 
Аналогично с этапом (доступные состояния DRAFT, PLANNED, IN_PROGRESS).
Участник не может быть привязан к двум раундам этапа одновременно.
ParticipantController.addParticipantToRound
ParticipantController.removeParticipantFromRound
ParticipantController.addParticipantToMilestone
ParticipantController.removeParticipantFromMilestone

Далее старт активности (IN_PROGRESS) - 1 этап (IN_PROGRESS) - остальные (PENDING)
Админ стартует раунд IN_PROGRESS
если нужно добавить участника
если уже стартовали активность, этап и какой то раунд, но нужно добавить еще одного учасника в этот же этап, который не был ранее зарегистрирован
т.е. это условный Кевин, который решил поучаствовать в последний момент, когда уже всё идет и ему побыстрому выдают номер,
т.е. его надо добавить как участника + зарегистриовать, и добавить в этап и раунд.  
Т.е. добавление, регистрация участника возможна в любом незавершенном состоянии активности, этапа.
Раунд в DRAFT JudgeRoundStatus (NOT_READY), JudgeMilestoneStatus (NOT_READY), предыдущие JudgeMilestoneResult по данному раунду удаляются если они были - addParticipantToMilestone - addParticipantToRound -
Раунд в PLANNED или IN_PROGRESS

Сохраняем JudgeMilestoneResult для раунда

Когда все JudgeRoundStatus READY для одного судьи то раунд переходит в READY
если кто-то меняет на JudgeRoundStatus (NOT_READY) то раунд в IN_PROGRESS, JudgeMilestoneStatus (NOT_READY) для данного судьи
