package org.bn.sensation.entity;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "competition")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Competition extends BaseEntity{

    @Column(nullable = false, name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(nullable = false, name = "start_date")
    private Instant startDate;

    @ManyToMany
    @JoinTable(
            name = "JUDGES_TO_COMPETITION",
            joinColumns = @JoinColumn(name = "JUDGE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "COMPETITION_ID", referencedColumnName = "ID")
    )
    private List<Judge> judges;
}

