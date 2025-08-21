package de.secretj12.turnierplaner.db.entities.groups;

import de.secretj12.turnierplaner.db.entities.Match;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "final_of_group")
public class FinalOfGroup {

    @Id
    private UUID id;

    @Column(name = "position", nullable = false)
    private int pos;

    @MapsId
    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "next_match", referencedColumnName = "id", nullable = false)
    private Match nextMatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_a", referencedColumnName = "id", nullable = false)
    private Group groupA;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_b", referencedColumnName = "id", nullable = false)
    private Group groupB;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Match getNextMatch() {
        return nextMatch;
    }

    public void setNextMatch(Match nextMatch) {
        this.nextMatch = nextMatch;
    }

    public Group getGroupA() {
        return groupA;
    }

    public void setGroupA(Group groupA) {
        this.groupA = groupA;
    }

    public Group getGroupB() {
        return groupB;
    }

    public void setGroupB(Group groupB) {
        this.groupB = groupB;
    }
}
