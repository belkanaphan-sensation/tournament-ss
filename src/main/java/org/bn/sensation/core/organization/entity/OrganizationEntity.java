package org.bn.sensation.core.organization.entity;

import java.util.HashSet;
import java.util.Set;

import org.bn.sensation.core.common.entity.Address;
import org.bn.sensation.core.common.entity.BaseEntity;
import org.bn.sensation.core.occasion.entity.OccasionEntity;
import org.bn.sensation.core.user.entity.UserEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "organization")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationEntity extends BaseEntity {

    @Column(name = "name")
    private String name; // Название организации

    @Column(name = "description")
    private String description;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Embedded
    private Address address;

    @ManyToMany(mappedBy = "organizations")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Builder.Default
    private Set<UserEntity> users = new HashSet<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.REMOVE)
    @Builder.Default
    private Set<OccasionEntity> occasions = new HashSet<>();
}
