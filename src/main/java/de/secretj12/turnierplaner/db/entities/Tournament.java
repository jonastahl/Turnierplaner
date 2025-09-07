package de.secretj12.turnierplaner.db.entities;

import de.secretj12.turnierplaner.db.entities.competition.Competition;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tournaments")
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", unique = true)
    private String name;
    @Column(name = "description")
    private String description;

    @Column(name = "begin_registration")
    private Instant beginRegistration;
    @Column(name = "end_registration")
    private Instant endRegistration;

    @Column(name = "begin_game_phase")
    private Instant beginGamePhase;
    @Column(name = "end_game_phase")
    private Instant endGamePhase;

    @Column(name = "visible")
    private boolean visible;

    @ManyToMany
    @JoinTable(
               name = "court_of_tournament",
               joinColumns = {@JoinColumn(name = "id")},
               inverseJoinColumns = {@JoinColumn(name = "name")}
    )
    private Set<Court> courts;

    @OneToMany(mappedBy = "tournament", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<Competition> competitions;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getBeginRegistration() {
        return beginRegistration;
    }

    public void setBeginRegistration(Instant beginRegistration) {
        this.beginRegistration = beginRegistration;
    }

    public Instant getEndRegistration() {
        return endRegistration;
    }

    public void setEndRegistration(Instant endRegistration) {
        this.endRegistration = endRegistration;
    }

    public Instant getBeginGamePhase() {
        return beginGamePhase;
    }

    public void setBeginGamePhase(Instant beginGamePhase) {
        this.beginGamePhase = beginGamePhase;
    }

    public Instant getEndGamePhase() {
        return endGamePhase;
    }

    public void setEndGamePhase(Instant endGamePhase) {
        this.endGamePhase = endGamePhase;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Set<Court> getCourts() {
        return courts;
    }

    public void setCourts(Set<Court> courts) {
        this.courts = courts;
    }

    public Set<Competition> getCompetitions() {
        return competitions;
    }

    public void setCompetitions(Set<Competition> competitions) {
        this.competitions = competitions;
    }
}
