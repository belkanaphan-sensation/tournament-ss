package org.bn.sensation.core.milestoneresult.service.mapper;

import org.bn.sensation.core.common.mapper.BaseDtoMapper;
import org.bn.sensation.core.milestoneresult.entity.MilestoneResultEntity;
import org.bn.sensation.core.milestoneresult.service.dto.UpdateMilestoneResultRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = BaseDtoMapper.class)
public interface UpdateMilestoneResultRequestMapper extends BaseDtoMapper<MilestoneResultEntity, UpdateMilestoneResultRequest> {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateMilestoneResultFromRequest(UpdateMilestoneResultRequest request, @MappingTarget MilestoneResultEntity entity);
}
