package de.secretj12.turnierplaner.db.entities.competition;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.enums.*;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.groups.Group;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "competitions")
@NamedQueries({
               @NamedQuery(name = "compByName", query = """
                   SELECT c FROM Competition c LEFT JOIN Tournament t ON c.tournament = t
                   WHERE t.name = ?1 AND c.name = ?2"""),
               @NamedQuery(name = "listByName", query = """
                   SELECT c FROM Competition c LEFT JOIN Tournament t ON c.tournament = t WHERE t.name=  ?1""")})
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Fetch(FetchMode.SELECT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Tournament tournament;

    @Column(name = "description")
    private String description;
    @Column(name = "type", nullable = false)
    private CompetitionType type;

    @Column(name = "mode", nullable = false)
    private CompetitionMode mode;
    @Column(name = "signup", nullable = false)
    private CompetitionSignUp signup;

    @Column(name = "number_sets", nullable = false)
    private NumberSets numberSets;

    @Column(name = "playerA_sex", nullable = false)
    private SexFilter playerASexFilter;
    @Column(name = "playerA_has_min_age")
    private boolean playerAhasMinAge;
    @Column(name = "playerA_min_age")
    private LocalDate playerAminAge;
    @Column(name = "playerA_has_max_age")
    private boolean playerAhasMaxAge;
    @Column(name = "playerA_max_age")
    private LocalDate playerAmaxAge;

    @Column(name = "playerB_different")
    private boolean playerBdifferent;
    @Column(name = "playerB_sex")
    private SexFilter playerBSexFilter;
    @Column(name = "playerB_has_min_age")
    private boolean playerBhasMinAge;
    @Column(name = "playerB_min_age")
    private LocalDate playerBminAge;
    @Column(name = "playerB_has_max_age")
    private boolean playerBhasMaxAge;
    @Column(name = "playerB_max_age")
    private LocalDate playerBmaxAge;
    @Column(name = "creation_progess", nullable = false)
    private CreationProgress cProgress;

    @Column(name = "totalRounds")
    private int totalRounds;

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finale_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Match finale;
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thirdPlace_id")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Match thirdPlace;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Team> teams;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Match> matches;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Group> groups;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CompetitionType getType() {
        return type;
    }

    public void setType(CompetitionType type) {
        this.type = type;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public List<Group> getGroups() {
        return groups.stream().sorted(Comparator.comparingLong(Group::getIndex)).collect(Collectors.toList());
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public CompetitionMode getMode() {
        return mode;
    }

    public void setMode(CompetitionMode mode) {
        this.mode = mode;
    }

    public CompetitionSignUp getSignup() {
        return signup;
    }

    public void setSignup(CompetitionSignUp signup) {
        this.signup = signup;
    }

    public SexFilter getPlayerASex() {
        return playerASexFilter;
    }

    public void setPlayerASex(SexFilter playerASexFilter) {
        this.playerASexFilter = playerASexFilter;
    }

    public boolean playerAhasMinAge() {
        return playerAhasMinAge;
    }

    public void setPlayerAhasMinAge(boolean playerAhasMinAge) {
        this.playerAhasMinAge = playerAhasMinAge;
    }

    public LocalDate getPlayerAminAge() {
        return playerAminAge;
    }

    public void setPlayerAminAge(LocalDate playerAminAge) {
        this.playerAminAge = playerAminAge;
    }

    public boolean playerAhasMaxAge() {
        return playerAhasMaxAge;
    }

    public void setPlayerAhasMaxAge(boolean playerAhasMaxAge) {
        this.playerAhasMaxAge = playerAhasMaxAge;
    }

    public LocalDate getPlayerAmaxAge() {
        return playerAmaxAge;
    }

    public void setPlayerAmaxAge(LocalDate playerAmaxAge) {
        this.playerAmaxAge = playerAmaxAge;
    }

    public boolean isPlayerBdifferent() {
        return playerBdifferent;
    }

    public void setPlayerBdifferent(boolean playerBequalsA) {
        this.playerBdifferent = playerBequalsA;
    }

    public SexFilter getPlayerBSex() {
        return playerBSexFilter;
    }

    public void setPlayerBSex(SexFilter playerBSexFilter) {
        this.playerBSexFilter = playerBSexFilter;
    }

    public boolean playerBhasMinAge() {
        return playerBhasMinAge;
    }

    public void setPlayerBhasMinAge(boolean playerBhasMinAge) {
        this.playerBhasMinAge = playerBhasMinAge;
    }

    public LocalDate getPlayerBminAge() {
        return playerBminAge;
    }

    public void setPlayerBminAge(LocalDate playerBminAge) {
        this.playerBminAge = playerBminAge;
    }

    public boolean playerBhasMaxAge() {
        return playerBhasMaxAge;
    }

    public void setPlayerBhasMaxAge(boolean playerBhasMaxAge) {
        this.playerBhasMaxAge = playerBhasMaxAge;
    }

    public LocalDate getPlayerBmaxAge() {
        return playerBmaxAge;
    }

    public void setPlayerBmaxAge(LocalDate playerBmaxAge) {
        this.playerBmaxAge = playerBmaxAge;
    }

    public CreationProgress getcProgress() {
        return cProgress;
    }

    public void setcProgress(CreationProgress cProgress) {
        this.cProgress = cProgress;
    }

    public Match getThirdPlace() {
        return thirdPlace;
    }

    public void setThirdPlace(Match thirdPlace) {
        this.thirdPlace = thirdPlace;
    }

    public Match getFinale() {
        return finale;
    }

    public void setFinale(Match finale) {
        this.finale = finale;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int total) {
        this.totalRounds = total;
    }

    public NumberSets getNumberSets() {
        return numberSets;
    }

    public void setNumberSets(NumberSets numberSets) {
        this.numberSets = numberSets;
    }

    public SexFilter getPlayerASexFilter() {
        return playerASexFilter;
    }

    public void setPlayerASexFilter(SexFilter playerASexFilter) {
        this.playerASexFilter = playerASexFilter;
    }

    public boolean isPlayerAhasMinAge() {
        return playerAhasMinAge;
    }

    public boolean isPlayerAhasMaxAge() {
        return playerAhasMaxAge;
    }

    public SexFilter getPlayerBSexFilter() {
        return playerBSexFilter;
    }

    public void setPlayerBSexFilter(SexFilter playerBSexFilter) {
        this.playerBSexFilter = playerBSexFilter;
    }

    public boolean isPlayerBhasMinAge() {
        return playerBhasMinAge;
    }

    public boolean isPlayerBhasMaxAge() {
        return playerBhasMaxAge;
    }
}
