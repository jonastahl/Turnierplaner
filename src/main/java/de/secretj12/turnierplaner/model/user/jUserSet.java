package de.secretj12.turnierplaner.model.user;

import de.secretj12.turnierplaner.db.entities.Set;

public class jUserSet {
    private byte scoreA;
    private byte scoreB;

    public jUserSet() {
    }

    public jUserSet(byte scoreA, byte scoreB) {
        this.scoreA = scoreA;
        this.scoreB = scoreB;
    }

    public jUserSet(Set set) {
        this.scoreA = set.getScoreA();
        this.scoreB = set.getScoreB();
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
}
