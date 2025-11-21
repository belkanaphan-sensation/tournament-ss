package org.bn.sensation.core.allowedaction.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activity.repository.ActivityRepository;
import org.bn.sensation.core.activity.statemachine.ActivityEvent;
import org.bn.sensation.core.allowedaction.service.dto.AllowedActionDto;
import org.bn.sensation.core.common.statemachine.policy.TransitionAvailabilityService;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.repository.MilestoneRepository;
import org.bn.sensation.core.milestone.statemachine.MilestoneEvent;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.occasion.repository.OccasionRepository;
import org.bn.sensation.core.occasion.statemachine.OccasionEvent;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllowedActionServiceImpl implements AllowedActionService {

    private final CurrentUser currentUser;
    private final ActivityRepository activityRepository;
    private final MilestoneRepository milestoneRepository;
    private final OccasionRepository occasionRepository;
    private final TransitionAvailabilityService transitionAvailabilityService;

    @Override
    @Transactional(readOnly = true)
    public AllowedActionDto getForOccasion(Long occasionId) {
        Role currentRole = currentUser.getSecurityUser().getCurrentRole();
        OccasionEntity occasion = occasionRepository.getByIdOrThrow(occasionId);
        log.debug("Calculating allowed actions for occasionId={} role={}", occasionId, currentRole);

        Set<OccasionEvent> allowedStates = transitionAvailabilityService.findAllowedStates(occasion, currentRole);

        return new AllowedActionDto(getEntityName(OccasionEntity.class.getSimpleName()), allowedStates.stream()
                .map(Enum::name)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    @Override
    @Transactional(readOnly = true)
    public AllowedActionDto getForActivity(Long activityId) {
        Role currentRole = currentUser.getSecurityUser().getCurrentRole();
        ActivityEntity activity = activityRepository.getByIdOrThrow(activityId);
        log.debug("Calculating allowed actions for activityId={} role={}", activityId, currentRole);

        Set<ActivityEvent> allowedStates = transitionAvailabilityService.findAllowedStates(activity, currentRole);

        return new AllowedActionDto(getEntityName(ActivityEntity.class.getSimpleName()), allowedStates.stream()
                .map(Enum::name)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    @Override
    @Transactional(readOnly = true)
    public AllowedActionDto getForMilestone(Long milestoneId) {
        Role currentRole = currentUser.getSecurityUser().getCurrentRole();
        MilestoneEntity milestone = milestoneRepository.getByIdFullOrThrow(milestoneId);
        log.debug("Calculating allowed actions for milestoneId={} role={}", milestoneId, currentRole);

        Set<MilestoneEvent> allowedStates = transitionAvailabilityService.findAllowedStates(milestone, currentRole);

        return new AllowedActionDto(getEntityName(MilestoneEntity.class.getSimpleName()), allowedStates.stream()
                .map(Enum::name)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private static String getEntityName(String classSimpleName) {
        return classSimpleName.replace("Entity", "");
    }
}
