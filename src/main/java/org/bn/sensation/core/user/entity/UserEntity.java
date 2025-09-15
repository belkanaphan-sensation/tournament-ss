package org.bn.sensation.core.user.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.organization.entity.OrganizationEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ElementCollection(targetClass = Role.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Embedded
    private Person person;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_organizations_association",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Builder.Default
    private Set<OrganizationEntity> organizations = new HashSet<>();
}
