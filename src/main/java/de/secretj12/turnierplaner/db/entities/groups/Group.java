package de.secretj12.turnierplaner.db.entities.groups;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;
    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE)
    private Set<MatchOfGroup> matchesOfGroup;
    @Column(name = "index")
    private byte index;
    @ManyToOne
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @OneToMany(mappedBy = "groupA", cascade = CascadeType.REMOVE)
    private Set<FinalOfGroup> finalOfGroupA;
    @OneToMany(mappedBy = "groupB", cascade = CascadeType.REMOVE)
    private Set<FinalOfGroup> finalOfGroupB;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public Set<MatchOfGroup> getMatchesOfGroup() {
        return matchesOfGroup;
    }

    public void setMatchesOfGroup(Set<MatchOfGroup> matchesOfGroup) {
        this.matchesOfGroup = matchesOfGroup;
    }

    public Set<Match> getMatches() {
        if (matchesOfGroup == null)
            return Set.of();

        return matchesOfGroup.stream().map(MatchOfGroup::getMatch).collect(Collectors.toCollection(HashSet::new));
    }

    public Set<FinalOfGroup> getFinalOfGroupA() {
        return finalOfGroupA;
    }

    public void setFinalOfGroupA(Set<FinalOfGroup> finalOfGroupA) {
        this.finalOfGroupA = finalOfGroupA;
    }

    public Set<FinalOfGroup> getFinalOfGroupB() {
        return finalOfGroupB;
    }

    public void setFinalOfGroupB(Set<FinalOfGroup> finalOfGroupB) {
        this.finalOfGroupB = finalOfGroupB;
    }
}
