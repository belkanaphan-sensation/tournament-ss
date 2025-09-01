package org.bn.sensation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bn.sensation.common.entity.BaseEntity;

@Entity
@Table(name = "criteria")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Criteria extends BaseEntity {

    @Column(unique = true, nullable = false, name = "criteria")
    private String criteria;
}
