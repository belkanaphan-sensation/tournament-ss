package org.bn.sensation.core.milestone.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Запрос на формирование раундов с конкурсантами")
public class PrepareRoundsRequest {

//    @Schema(description = "Список ID конкурсантов. Если он присутствует, " +
//            "то конкурсанты добавляются из данного списка, если нет, то все конкурсанты, " +
//            "зарегистрированные в активности, которой принадлежит этап")
//    private List<Long> contestantIds;

    @Schema(description = "Количество конкурсантов в заходе (раунде)" +
            " (если оно отличается от того что было задано изначально)")
    private Integer roundContestantLimit;
}
