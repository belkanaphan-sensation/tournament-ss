package org.bn.sensation.core.contestant.service;

import java.util.*;
import java.util.stream.Collectors;

import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.activityuser.service.ActivityUserUtil;
import org.bn.sensation.core.common.entity.PartnerSide;
import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.contestant.entity.ContestantEntity;
import org.bn.sensation.core.contestant.entity.ContestantType;
import org.bn.sensation.core.contestant.repository.ContestantRepository;
import org.bn.sensation.core.contestant.service.dto.ContestantDto;
import org.bn.sensation.core.contestant.service.dto.CreateContestantRequest;
import org.bn.sensation.core.contestant.service.mapper.ContestantDtoMapper;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.statemachine.MilestoneState;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.participant.repository.ParticipantRepository;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.repository.RoundRepository;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestantServiceImpl implements ContestantService {

    private final CurrentUser currentUser;
    private final ContestantRepository contestantRepository;
    private final ContestantDtoMapper contestantDtoMapper;
    private final MilestoneRepository milestoneRepository;
    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;

    @Override
    public BaseRepository<ContestantEntity> getRepository() {
        return contestantRepository;
    }

    @Override
    public BaseDtoMapper<ContestantEntity, ContestantDto> getMapper() {
        return contestantDtoMapper;
    }

    @Override
    @Transactional
    public ContestantDto create(CreateContestantRequest request) {
        MilestoneEntity milestone = milestoneRepository.getByIdWithRuleOrThrow(request.getMilestoneId());
        Preconditions.checkState(Set.of(MilestoneState.DRAFT, MilestoneState.PLANNED, MilestoneState.PENDING).contains(milestone.getState()),
                "Конкурсант не может быть создан т.к. этап с состоянием %s", milestone.getState());
        RoundEntity round = request.getRoundId() != null ? roundRepository.getByIdOrThrow(request.getRoundId()) : null;
        if (round != null) {
            Preconditions.checkArgument(round.getMilestone().getId().equals(milestone.getId()), "Раунд %s не принадлежит этапу %s", round.getId(), milestone.getId());
        }
        List<ParticipantEntity> participants = participantRepository.findAllByIdFull(request.getParticipantIds());
        Preconditions.checkArgument(milestone.getMilestoneRule().getContestantType().getParticipantCount() == participants.size()
                        && milestone.getMilestoneRule().getContestantType().getParticipantCount() == participants.stream().map(ParticipantEntity::getPartnerSide).distinct().count(),
                "Некорректное количество участников");
        Preconditions.checkArgument(participants.stream().allMatch(ParticipantEntity::getIsRegistered),
                "Нельзя использовать незарегистрированных участников");
        Preconditions.checkArgument(participants.stream().allMatch(p -> p.getActivity().getId().equals(milestone.getActivity().getId())),
                "Участники относятся к другой активности");
        Preconditions.checkArgument(milestone.getContestants().stream().flatMap(c -> c.getParticipants().stream()).noneMatch(participants::contains),
                "Данные участники уже являются конкурсантами в данном этапе");
        ContestantEntity entity = new ContestantEntity();
        entity.setContestantType(milestone.getMilestoneRule().getContestantType());
        entity.getParticipants().addAll(participants);
        entity.getMilestones().add(milestone);
        if (round != null) {
            entity.getRounds().add(round);
        }
        entity.setNumber(getNumber(milestone, participants));

        return contestantDtoMapper.toDto(contestantRepository.save(entity));
    }

    private String getNumber(MilestoneEntity milestone, List<ParticipantEntity> participants) {
        return milestone.getMilestoneRule().getContestantType() == ContestantType.SINGLE
                ? participants.iterator().next().getNumber()
                : "#" + (milestone.getContestants().size() + 1) + participants.stream()
                .sorted(Comparator.comparing(ParticipantEntity::getPartnerSide))
                .map(ParticipantEntity::getNumber)
                .collect(Collectors.joining(":", " (", ")"));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<ContestantEntity> createContestants(MilestoneEntity milestone, List<ParticipantEntity> participants) {
        ContestantType type = milestone.getMilestoneRule().getContestantType();
        List<ContestantEntity> contestants = switch (type) {
            case SINGLE -> participants.stream()
                    .map(p -> {
                        ContestantEntity entity = new ContestantEntity();
                        entity.setNumber(p.getNumber());
                        entity.setContestantType(type);
                        entity.getParticipants().add(p);
                        entity.getMilestones().add(milestone);
                        p.getContestants().add(entity);
                        milestone.getContestants().add(entity);
                        return entity;
                    })
                    .toList();
            case COUPLE_TRANSIENT, COUPLE_PERSISTENT -> {
                MilestoneEntity previousMilestone = milestoneRepository.findByActivityIdAndMilestoneOrder(milestone.getActivity().getId(), milestone.getMilestoneOrder() + 1).orElse(null);
                Map<Long, Long> previousCouples = new HashMap<>();
                if (previousMilestone != null
                        && previousMilestone.getState() != MilestoneState.SKIPPED
                        && previousMilestone.getMilestoneRule().getContestantType() == milestone.getMilestoneRule().getContestantType()) {
                    previousMilestone.getContestants().forEach(c -> {
                        Iterator<ParticipantEntity> iterator = c.getParticipants().iterator();
                        ParticipantEntity p1 = iterator.next();
                        ParticipantEntity p2 = iterator.next();
                        previousCouples.put(p1.getId(), p2.getId());
                        previousCouples.put(p2.getId(), p1.getId());
                    });
                }
                List<ParticipantEntity> leaders = new ArrayList<>(participants.stream().filter(p -> p.getPartnerSide() == PartnerSide.LEADER).toList());
                List<ParticipantEntity> followers = new ArrayList<>(participants.stream().filter(p -> p.getPartnerSide() == PartnerSide.FOLLOWER).toList());
                Preconditions.checkArgument(leaders.size() == followers.size(), "Количество партнеров и партнерш должно быть одинаковое");
                Collections.shuffle(leaders);
                Collections.shuffle(followers);
                List<ContestantEntity> c = new ArrayList<>();
                for (int i = 0; i < leaders.size(); i++) {
                    ParticipantEntity leader = leaders.get(i);
                    ParticipantEntity follower = followers.remove(0);
                    Long prevFollowerId = previousCouples.get(leader.getId());
                    if (prevFollowerId != null && follower.getId().equals(prevFollowerId)) {
                        followers.add(follower);
                        follower = followers.remove(0);
                    }
                    ContestantEntity entity = new ContestantEntity();
                    entity.setNumber(getNumber(milestone, List.of(leader, follower)));
                    entity.setContestantType(type);
                    entity.getParticipants().add(leader);
                    entity.getParticipants().add(follower);
                    entity.getMilestones().add(milestone);
                    leader.getContestants().add(entity);
                    follower.getContestants().add(entity);
                    milestone.getContestants().add(entity);
                    c.add(entity);
                }
                yield c;
            }
        };
        return contestantRepository.saveAll(contestants);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        ContestantEntity contestant = contestantRepository.getByIdOrThrow(id);
        Preconditions.checkState(contestant.getMilestones().stream()
                        .allMatch(m -> Set.of(MilestoneState.DRAFT, MilestoneState.PLANNED, MilestoneState.PENDING).contains(m.getState())),
                "Нельзя удалить конкурсанта, т.к. этапы в неподходящем состоянии");
        contestantRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContestantDto> findByRoundId(Long roundId) {
        return contestantRepository.findByRoundId(roundId).stream()
                .map(contestantDtoMapper::toDto)
                .sorted(Comparator.comparing(p -> p.getNumber()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContestantDto> getByRoundByRoundIdForCurrentUser(Long roundId) {
        log.info("Получение конкурсантов раунда={} для текущего пользователя={}", roundId, currentUser.getSecurityUser().getId());
        RoundEntity round = roundRepository.getByIdFullOrThrow(roundId);
        ContestantType contestantType = round.getMilestone().getMilestoneRule().getContestantType();
        Long userId = currentUser.getSecurityUser().getId();
        ActivityUserEntity activityUser = ActivityUserUtil.getFromActivity(
                round.getMilestone().getActivity(), userId, uaa -> uaa.getUser().getId().equals(userId));
        log.debug("Найден activity user={} для раунда={}, сторона={}", userId, round.getId(), activityUser.getPartnerSide());
        return contestantRepository.findByRoundId(roundId).stream()
                .filter(c -> {
                    if (contestantType == ContestantType.SINGLE && activityUser.getPartnerSide() != null) {
                        boolean matches = c.getParticipants().iterator().next().getPartnerSide() == activityUser.getPartnerSide();
                        log.debug("Конкурсант={} со стороной={} соответствует стороне судьи={}: {}",
                                c.getId(), c.getParticipants().iterator().next().getPartnerSide(), activityUser.getPartnerSide(), matches);
                        return matches;
                    }
                    return true;
                })
                .map(contestantDtoMapper::toDto)
                .sorted(Comparator.comparing(p -> p.getNumber()))
                .toList();
    }
}
