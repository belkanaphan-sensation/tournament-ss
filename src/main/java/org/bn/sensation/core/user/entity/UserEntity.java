package org.bn.sensation.core.user.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.bn.sensation.core.role.entity.RoleEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_role_association",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Embedded
    private Person person;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;
}
