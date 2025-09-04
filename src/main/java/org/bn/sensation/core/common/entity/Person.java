package org.bn.sensation.core.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "second_name")
    private String secondName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;
}
