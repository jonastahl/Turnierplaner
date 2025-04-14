package de.secretj12.turnierplaner.db.repositories;

import de.secretj12.turnierplaner.db.entities.Match;
import de.secretj12.turnierplaner.db.entities.Match_;
import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.db.entities.Tournament;
import de.secretj12.turnierplaner.db.entities.competition.Competition;
import de.secretj12.turnierplaner.db.entities.competition.Competition_;
import de.secretj12.turnierplaner.db.entities.competition.Team_;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MatchRepository implements PanacheRepository<Match> {
    @Inject
    NextMatchRepository nextMatches;
    @Inject
    FinalOfGroupRepository finalOfGroups;
    @Inject
    MatchOfGroupRepository matchOfGroups;
    @Inject
    GroupRepository groups;


    public Match findById(UUID id) {
        return find("id", id).firstResult();
    }

    public void deleteByComp(Competition competition) {
        if (competition.getFinale() != null)
            deleteMatch(competition.getFinale());
        competition.setFinale(null);
        competition.setThirdPlace(null);
        delete("#deleteByComp", Parameters.with("comp", competition));
    }

    private void deleteMatch(Match m) {
        if (m.getDependentOn() != null) {
            deleteMatch(m.getDependentOn().getPreviousA());
            deleteMatch(m.getDependentOn().getPreviousB());
        } else {
            delete(m);
        }
    }

    public List<Match> nonGroupMatches(Competition competition) {
        return find("#nonGroupMatches", Parameters.with("comp", competition)).list();
    }

    public List<Match> filterMatches(Tournament tournament, Competition competition,
                                     Player player,
                                     Instant from, Instant to) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Match> q = cb.createQuery(Match.class);
        Root<Match> root = q.from(Match.class);
        q.select(root);
        Predicate predicate = applyAllValidPredicates(cb, root, tournament, competition, player, from, to);
        q.where(predicate);

        return getEntityManager().createQuery(q).getResultList();
    }

    private Predicate applyAllValidPredicates(CriteriaBuilder cb, Root<Match> root,
                                              Tournament tournament, Competition competition, Player player,
                                              Instant from, Instant to) {
        List<Predicate> predicates = new ArrayList<>();
        if (competition != null) {
            predicates.add(cb.equal(root.get(Match_.COMPETITION), competition));
        }
        if (tournament != null) {
            predicates.add(cb.equal(root.get(Match_.COMPETITION).get(Competition_.TOURNAMENT), tournament));
        }
        if (player != null) {
            predicates.add(cb.or(
                cb.equal(root.get(Match_.TEAM_A).get(Team_.PLAYER_A), player),
                cb.equal(root.get(Match_.TEAM_A).get(Team_.PLAYER_B), player),
                cb.equal(root.get(Match_.TEAM_B).get(Team_.PLAYER_A), player),
                cb.equal(root.get(Match_.TEAM_B).get(Team_.PLAYER_B), player)
            ));
        }
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(Match_.BEGIN), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(Match_.END), to));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}