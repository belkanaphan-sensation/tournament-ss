package org.bn.sensation.core.round.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.criteria.entity.MilestoneCriteriaAssignmentEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.round.entity.RoundResultEntity;
import org.bn.sensation.core.round.service.dto.CreateRoundResultRequest;
import org.bn.sensation.core.user.entity.UserActivityAssignmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface CreateRoundResultRequestMapper extends BaseDtoMapper<RoundResultEntity, CreateRoundResultRequest> {

    @Override
    @Mapping(target = "participant", source = "participantId")
    @Mapping(target = "round", source = "roundId")
    @Mapping(target = "milestoneCriteria", source = "milestoneCriteriaId")
    @Mapping(target = "activityUser", source = "activityUserId")
    RoundResultEntity toEntity(CreateRoundResultRequest dto);

    @Override
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "roundId", source = "round.id")
    @Mapping(target = "milestoneCriteriaId", source = "milestoneCriteria.id")
    @Mapping(target = "activityUserId", source = "activityUser.id")
    CreateRoundResultRequest toDto(RoundResultEntity entity);

    default ParticipantEntity mapParticipant(Long participantId) {
        if (participantId == null) {
            return null;
        }
        ParticipantEntity participant = new ParticipantEntity();
        participant.setId(participantId);
        return participant;
    }

    default RoundEntity mapRound(Long roundId) {
        if (roundId == null) {
            return null;
        }
        RoundEntity round = new RoundEntity();
        round.setId(roundId);
        return round;
    }

    default MilestoneCriteriaAssignmentEntity mapMilestoneCriteria(Long milestoneCriteriaId) {
        if (milestoneCriteriaId == null) {
            return null;
        }
        MilestoneCriteriaAssignmentEntity milestoneCriteria = new MilestoneCriteriaAssignmentEntity();
        milestoneCriteria.setId(milestoneCriteriaId);
        return milestoneCriteria;
    }

    default UserActivityAssignmentEntity mapActivityUser(Long activityUserId) {
        if (activityUserId == null) {
            return null;
        }
        UserActivityAssignmentEntity activityUser = new UserActivityAssignmentEntity();
        activityUser.setId(activityUserId);
        return activityUser;
    }
}