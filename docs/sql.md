-- Для отправления на перетанцовку
select mr.participant_id as "ID участника",
p.partner_side    as сторона,
p.number          as "номер участника",
mrr.total_score   as "суммарный балл",
mrr.judge_passed  as "результаты судей",
r.name            as "название раунда",
mrr.round_id      as "ID раунда",
mr.milestone_id   as "ID этапа",
p.name            as имя,
p.surname         as фамилия
from milestone_round_result mrr
join public.milestone_result mr on mr.id = mrr.milestone_result_id
join public.participant p on p.id = mr.participant_id
join public.round r on r.id = mrr.round_id
where mrr.round_id in (7, 8)
and mrr.judge_passed <> 'PASSED'
order by p.partner_side, mrr.total_score desc;

{
"name": "Иван",
"surname": "Иванов",
"partnerSide": "LEADER",
"activityId": 1
}

{
"name": "Иван",
"surname": "Иванов",
"partnerSide": "FOLLOWER",
"activityId": 1
}