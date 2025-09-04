package org.bn.sensation.core.role.entity;

import org.bn.sensation.core.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "role")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity extends BaseEntity {

  @Column(name = "role", nullable = false, unique = true)
  @Enumerated(EnumType.STRING)
  private Role role;
}
