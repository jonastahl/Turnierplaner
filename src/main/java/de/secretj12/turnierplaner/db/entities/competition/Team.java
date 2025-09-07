package de.secretj12.turnierplaner.db.entities.competition;

import de.secretj12.turnierplaner.db.entities.Player;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "teams")
@NamedQueries({
               @NamedQuery(name = "findTeams",
                           query = """
                               FROM Team t
                               JOIN Match m ON (t = m.teamA OR t = m.teamB)
                               JOIN MatchOfGroup mog ON m = mog.match
                               JOIN Group g ON mog.group = g
                               WHERE g = :group""")})
public class Team extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition", referencedColumnName = "id")
    private Competition competition;

    @ManyToOne
    @JoinColumn(name = "player_a", referencedColumnName = "id")
    private Player playerA;
    @ManyToOne
    @JoinColumn(name = "player_b", referencedColumnName = "id")
    private Player playerB;

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

    public Player getPlayerA() {
        return playerA;
    }

    public void setPlayerA(Player playerA) {
        this.playerA = playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public void setPlayerB(Player playerB) {
        this.playerB = playerB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static Team findById(UUID id) {
        return find("id", id).firstResult();
    }

    public String getFullName() {
        return playerA.getFullName() + (playerB != null ? "\n" + playerB.getFullName() : "");
    }
}
