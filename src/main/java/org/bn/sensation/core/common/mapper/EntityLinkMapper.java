package org.bn.sensation.core.common.mapper;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.criterion.entity.CriterionEntity;
import org.bn.sensation.core.milestonecriterion.entity.MilestoneCriterionEntity;
import org.bn.sensation.core.judgeroundstatus.entity.JudgeRoundStatusEntity;
import org.bn.sensation.core.milestone.entity.MilestoneEntity;
import org.bn.sensation.core.milestone.entity.MilestoneRuleEntity;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.participant.entity.ParticipantEntity;
import org.bn.sensation.core.round.entity.RoundEntity;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;
import org.bn.sensation.core.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseDtoMapper.class)
public interface EntityLinkMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(ActivityEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(MilestoneEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(OccasionEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(OrganizationEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "number")
    EntityLinkDto toEntityLinkDto(ParticipantEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(RoundEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "username")
    EntityLinkDto toEntityLinkDto(UserEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "name")
    EntityLinkDto toEntityLinkDto(CriterionEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "criterion.name")
    EntityLinkDto toEntityLinkDto(MilestoneCriterionEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "user.username")
    EntityLinkDto toEntityLinkDto(ActivityUserEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "assessmentMode")
    EntityLinkDto toEntityLinkDto(MilestoneRuleEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "value", source = "status")
    EntityLinkDto toEntityLinkDto(JudgeRoundStatusEntity entity);
}
