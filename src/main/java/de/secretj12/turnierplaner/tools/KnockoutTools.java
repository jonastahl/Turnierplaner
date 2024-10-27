package de.secretj12.turnierplaner.tools;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.entities.competition.Team;
import de.secretj12.turnierplaner.db.entities.knockout.NextMatch;
import de.secretj12.turnierplaner.db.repositories.CompetitionRepository;
import de.secretj12.turnierplaner.db.repositories.MatchRepository;
import de.secretj12.turnierplaner.db.repositories.NextMatchRepository;
import de.secretj12.turnierplaner.db.repositories.TeamRepository;
import de.secretj12.turnierplaner.model.user.knockout.jUserKnockoutMatch;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class KnockoutTools {

    @Inject
    MatchRepository matches;
    @Inject
    NextMatchRepository nextMatches;
    @Inject
    TeamRepository teams;
    @Inject
    CompetitionRepository competitions;

    public void updateKnockoutTree(Competition competition, jUserKnockoutMatch tree) {
        Match finale = competition.getFinale();
        if (finale == null)
            finale = new Match();

        jUserKnockoutMatch curTree = tree;
        int total = 1;
        while (curTree.getPreviousA() != null) {
            curTree = curTree.getPreviousA();
            total++;
        }
        competition.setTotal(total);
        int tCount = countTeams(tree, total);

        competition.setFinale(updateKnockoutTree(competition, tree, finale, total - 1));
        competition.setThirdPlace(updateThirdPlace(competition, finale, total, tCount));

        competitions.persist(competition);
    }

    private int countTeams(jUserKnockoutMatch tree, int total) {
        int c = 0;
        if (tree.getPreviousA() != null)
            c += countTeams(tree.getPreviousA(), total - 1);
        if (tree.getPreviousB() != null)
            c += countTeams(tree.getPreviousB(), total - 1);

        if (tree.getPreviousA() == null) {
            if (total != 1)
                throw new BadRequestException("Invalid tree");
            if (tree.getTeamA() != null)
                c++;
            if (tree.getTeamB() != null)
                c++;
        }
        return c;
    }

    private Match updateKnockoutTree(Competition competition, jUserKnockoutMatch tree, Match match, int number) {
        match.setNumber(number);
        match.setCompetition(competition);
        matches.persist(match);

        if (tree.getPreviousA() == null) {
            match.setTeamA(tree.getTeamA() == null ? null : teams.findById(tree.getTeamA().getId()));
            match.setTeamB(tree.getTeamB() == null ? null : teams.findById(tree.getTeamB().getId()));
            deletePrevious(match);
        } else {
            Match a, b;
            if (match.getDependentOn() == null) {
                NextMatch nMatch = new NextMatch();
                a = updateKnockoutTree(competition, tree.getPreviousA(), new Match(), number - 1);
                b = updateKnockoutTree(competition, tree.getPreviousB(), new Match(), number - 1);
                nMatch.setPreviousA(a);
                nMatch.setPreviousB(b);
                nMatch.setNextMatch(match);
                nextMatches.persist(nMatch);
                match.setDependentOn(nMatch);
            } else {
                a = updateKnockoutTree(competition, tree.getPreviousA(), match.getDependentOn().getPreviousA(),
                    number - 1);
                b = updateKnockoutTree(competition, tree.getPreviousB(), match.getDependentOn().getPreviousB(),
                    number - 1);
            }

            if (tree.getPreviousA().getPreviousA() == null) {
                match.setTeamA(selectTeam(a));
                match.setTeamB(selectTeam(b));
            } else {
                match.setTeamA(null);
                match.setTeamB(null);
            }
        }

        matches.persist(match);
        return match;
    }

    private Team selectTeam(Match match) {
        if (match == null)
            return null;
        if (match.getTeamA() == null && match.getTeamB() != null)
            return match.getTeamB();
        if (match.getTeamB() == null && match.getTeamA() != null)
            return match.getTeamA();
        return null;
    }

    private Match updateThirdPlace(Competition competition, Match finale, int total, int tCount) {
        Match thirdPlace = competition.getThirdPlace();

        // cover deletion of third place
        if (tCount <= 2) {
            // third place will be deleted by deletePrevious
            return null;
        }
        if (tCount == 3) {
            if (thirdPlace != null)
                matches.delete(thirdPlace);
            return null;
        }

        if (thirdPlace == null) {
            // create third place if didn't exist before
            thirdPlace = new Match();
            thirdPlace.setNumber(total - 1);
            thirdPlace.setCompetition(competition);
            NextMatch nMatch = new NextMatch();
            nMatch.setWinner(false);
            nMatch.setNextMatch(thirdPlace);

            nMatch.setPreviousA(finale.getDependentOn().getPreviousA());
            nMatch.setPreviousB(finale.getDependentOn().getPreviousB());

            matches.persist(thirdPlace);
            nextMatches.persist(nMatch);
            return thirdPlace;
        } else {
            // update third place if already existing
            NextMatch nMatch = thirdPlace.getDependentOn();

            nMatch.setPreviousA(finale.getDependentOn().getPreviousA());
            nMatch.setPreviousB(finale.getDependentOn().getPreviousB());

            nextMatches.persist(nMatch);

            thirdPlace.setNumber(total - 1);
            return thirdPlace;
        }
    }

    private void deletePrevious(Match match) {
        if (match.getDependentOn() != null) {
            Match a = match.getDependentOn().getPreviousA();
            Match b = match.getDependentOn().getPreviousB();
            NextMatch nm = match.getDependentOn();

            deletePrevious(a);
            deletePrevious(b);
            nm.setNextMatch(null);
            match.setDependentOn(null);
            matches.delete(a);
            matches.delete(b);
        }
    }
}
