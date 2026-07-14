package de.secretj12.turnierplaner.db.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "sets")
public class Set {

    @EmbeddedId
    private SetKey key;

    @Column(name = "score_a")
    private byte scoreA;
    @Column(name = "score_b")
    private byte scoreB;

    public SetKey getKey() {
        return key;
    }

    public void setKey(SetKey key) {
        this.key = key;
    }

    public byte getScoreA() {
        return scoreA;
    }

    public void setScoreA(byte scoreA) {
        this.scoreA = scoreA;
    }

    public byte getScoreB() {
        return scoreB;
    }

    public void setScoreB(byte scoreB) {
        this.scoreB = scoreB;
    }

    @Embeddable
    public static class SetKey implements Serializable {
        @ManyToOne
        @JoinColumn(name = "match_id")
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
        @JsonIdentityReference(alwaysAsId = true)
        private Match match;
        @Column(name = "index")
        private byte index;

        public byte getIndex() {
            return index;
        }

        public void setIndex(byte id) {
            this.index = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SetKey setKey = (SetKey) o;
            return match.equals(setKey.match) && index == (setKey.index);
        }

        @Override
        public int hashCode() {
            return Objects.hash(match, index);
        }

        public Match getMatch() {
            return match;
        }

        public void setMatch(Match match) {
            this.match = match;
        }
    }
}
