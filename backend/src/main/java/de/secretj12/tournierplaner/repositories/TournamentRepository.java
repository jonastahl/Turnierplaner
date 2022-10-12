package de.secretj12.tournierplaner.repositories;

import de.secretj12.tournierplaner.entities.Tournament;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import de.secretj12.tournierplaner.entities.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TournamentRepository implements PanacheRepository<Tournament> {

    public Tournament getByName(String name) {
        return find("name", name).firstResultOptional().orElse(null);
    }


}
