package de.secretj12.turnierplaner.model.user;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.enums.CompetitionType;

public class jUserMatchEvent extends jUserMatch {

    /**
     * Name of the tournament
     */
    public String tourName;
    /**
     * Name of the competition
     */
    public String compName;
    /**
     * Type of the match in competition
     * -> also group competitions contain knockout matches
     */
    public CompetitionType type;
    /**
     * For knockout: round number
     * For group: group number
     */
    public int number;
    /**
     * Only set for knockout
     * Total number of rounds
     */
    public int total;

    /**
     * Only set for knockout
     * Set if the match is the final
     */
    public boolean isFinal;

    public jUserMatchEvent(Match match) {
        super(match);
        this.tourName = match.getCompetition().getTournament().getName();
        this.compName = match.getCompetition().getName();
        this.type = match.getGroup() != null ? CompetitionType.GROUPS : CompetitionType.KNOCKOUT;
        this.number = match.getNumber();
        this.total = match.getCompetition().getTotalRounds();
        if (match.getDependentOn() == null) {
            if (match.getFinalOfGroup() == null) {
                this.isFinal = true;
            } else {
                this.isFinal = match.getFinalOfGroup().getPos() == 1;
            }
        } else {
            this.isFinal = match.getDependentOn().isWinner();
        }
    }

    public jUserMatchEvent() {
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public CompetitionType getType() {
        return type;
    }

    public void setType(CompetitionType type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }
}
