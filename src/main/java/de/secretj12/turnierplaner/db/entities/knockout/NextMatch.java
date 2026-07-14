package de.secretj12.turnierplaner.db.entities.knockout;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.secretj12.turnierplaner.db.entities.Match;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "dependant_matches")
public class NextMatch {

    @Id
    private UUID id;

    @MapsId
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "next_match", referencedColumnName = "id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Match nextMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_a", referencedColumnName = "id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Match previousA;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_b", referencedColumnName = "id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Match previousB;

    @JoinColumn(name = "winner")
    private boolean winner = true;

    public Match getNextMatch() {
        return nextMatch;
    }

    public void setNextMatch(Match nextMatch) {
        this.nextMatch = nextMatch;
    }

    public Match getPreviousA() {
        return previousA;
    }

    public void setPreviousA(Match previousA) {
        this.previousA = previousA;
    }

    public Match getPreviousB() {
        return previousB;
    }

    public void setPreviousB(Match previousB) {
        this.previousB = previousB;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setWinner(boolean winner) {
        this.winner = winner;
    }
}
