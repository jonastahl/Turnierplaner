package de.secretj12.tournierplaner.resources.FormEntities;

import de.secretj12.tournierplaner.entities.Competition;
import de.secretj12.tournierplaner.entities.CompetitionType;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

public class ReducedCompetition {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;
    @Column(name = "type")
    private CompetitionType type;

    public ReducedCompetition(Competition competition) {
        this.id = competition.getId();
        this.name = competition.getName();
        this.description = competition.getDescription();
        this.type = competition.getType();
    }

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

    public CompetitionType getType() {
        return type;
    }

    public void setType(CompetitionType type) {
        this.type = type;
    }
}
