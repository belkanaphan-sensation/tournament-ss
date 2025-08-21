package org.bn.jj.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "judge")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Judge extends Human {

    @OneToOne
    private User user;
    @ManyToMany
    @JoinTable(
            name = "JUDGES_TO_COMPETITION",
            joinColumns = @JoinColumn(name = "JUDGE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "COMPETITION_ID", referencedColumnName = "ID")
    )
    List<Competition> competitions;
}
