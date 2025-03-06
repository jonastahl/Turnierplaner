package de.secretj12.turnierplaner.model.user;

import de.secretj12.turnierplaner.db.entities.Player;

import java.util.UUID;

public class jUserPlayer {
    private UUID id;
    private String firstName;
    private String lastName;

    public jUserPlayer(UUID id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public jUserPlayer(Player p) {
        this.id = p.getId();
        this.firstName = p.getFirstName();
        this.lastName = p.getLastName();
    }

    public jUserPlayer() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}