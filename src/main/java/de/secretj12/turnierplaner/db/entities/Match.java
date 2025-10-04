package de.secretj12.turnierplaner.db.entities;

import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.entities.competition.Team;
import de.secretj12.turnierplaner.db.entities.groups.FinalOfGroup;
import de.secretj12.turnierplaner.db.entities.groups.MatchOfGroup;
import de.secretj12.turnierplaner.db.entities.knockout.NextMatch;
import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@Entity
@Table(name = "matches")
@NamedQueries({
               @NamedQuery(name = "deleteByComp",
                           query = """
                               DELETE FROM Match m WHERE m.competition = :comp"""),
               @NamedQuery(name = "nonGroupMatches",
                           query = """
                               FROM Match m WHERE m.competition = :comp
                               AND NOT EXISTS (FROM MatchOfGroup mog WHERE mog.match = m)
                               """)})
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @NotNull
    @Fetch(FetchMode.SELECT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court")
    private Court court;

    @Column(name = "begin_time")
    private Instant begin;
    @Column(name = "end_time")
    private Instant end;

    @Column(name = "finished", nullable = false)
    private Boolean finished = false;
    /**
     * Determines who is the winner of the game
     * true - Team A
     * false - Team B
     */
    @Column(name = "winner")
    private Boolean winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a")
    private Team teamA;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b")
    private Team teamB;

    @Column(name = "number")
    private int number;

    @OneToOne(mappedBy = "nextMatch", cascade = CascadeType.REMOVE)
    private NextMatch dependentOn;

    @OneToMany(mappedBy = "previousA", cascade = CascadeType.REMOVE)
    private Collection<NextMatch> previousOfA;

    @OneToMany(mappedBy = "previousB", cascade = CascadeType.REMOVE)
    private Collection<NextMatch> previousOfB;

    @Fetch(FetchMode.SELECT)
    @OneToOne(mappedBy = "nextMatch", cascade = CascadeType.REMOVE)
    private FinalOfGroup finalOfGroup;

    @Fetch(FetchMode.SELECT)
    @OneToOne(mappedBy = "match", cascade = CascadeType.REMOVE)
    private MatchOfGroup group;

    @OneToMany(mappedBy = "key.match", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Set> sets;

    public static Match copy(Match match) {
        Match m = new Match();
        m.court = match.court;
        m.begin = match.begin;
        m.end = match.end;
        m.teamA = match.teamA;
        m.teamB = match.teamB;
        m.sets = match.sets;
        return m;
    }

    public Stream<Player> getPlayers() {
        return Stream.of(this.getTeamA(), this.getTeamB()).filter(Objects::nonNull)
            .flatMap(t -> Stream.of(t.getPlayerA(), t.getPlayerB())).filter(Objects::nonNull);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competitionId) {
        this.competition = competitionId;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court courts) {
        this.court = courts;
    }

    public Instant getBegin() {
        return begin;
    }

    public void setBegin(Instant begin) {
        this.begin = begin;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public Boolean isFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Boolean getWinner() {
        return winner;
    }

    public void setWinner(Boolean winner) {
        this.winner = winner;
    }

    public Team getTeamA() {
        return teamA;
    }

    public void setTeamA(Team teamA) {
        this.teamA = teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public void setTeamB(Team teamB) {
        this.teamB = teamB;
    }

    public NextMatch getDependentOn() {
        return dependentOn;
    }

    public void setDependentOn(NextMatch dependentOn) {
        this.dependentOn = dependentOn;
    }

    public Collection<NextMatch> getPreviousOfA() {
        return previousOfA;
    }

    public void setPreviousOfA(Collection<NextMatch> previousOfA) {
        this.previousOfA = previousOfA;
    }

    public Collection<NextMatch> getPreviousOfB() {
        return previousOfB;
    }

    public void setPreviousOfB(Collection<NextMatch> previousOfB) {
        this.previousOfB = previousOfB;
    }

    public FinalOfGroup getFinalOfGroup() {
        return finalOfGroup;
    }

    public void setFinalOfGroup(FinalOfGroup finalOfGroup) {
        this.finalOfGroup = finalOfGroup;
    }

    public MatchOfGroup getGroup() {
        return group;
    }

    public void setGroup(MatchOfGroup group) {
        this.group = group;
    }

    public List<Set> getSets() {
        return sets == null ? List.of() : sets.stream().sorted(Comparator.comparingInt(s -> s.getKey().getIndex()))
            .toList();
    }

    public void setSets(List<Set> sets) {
        this.sets = sets;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
