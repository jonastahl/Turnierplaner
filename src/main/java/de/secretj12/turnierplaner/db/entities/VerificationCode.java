package de.secretj12.turnierplaner.db.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verificationcodes")
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "expiration_date")
    private Instant expiration_date;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Instant getExpirationDate() {
        return expiration_date;
    }

    public void setExpirationDate(Instant expiration_date) {
        this.expiration_date = expiration_date;
    }
}
