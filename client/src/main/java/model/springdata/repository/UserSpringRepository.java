package model.springdata.repository;

import model.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring-Data-Repository für {@link User Benutzer}.
 * <p>
 * Erbt die üblichen CRUD-Operationen von {@link JpaRepository} und ergänzt
 * eine Suche anhand des Benutzernamens.
 */
public interface UserSpringRepository extends JpaRepository<User, Long> {

    /**
     * Sucht einen Benutzer anhand seines Benutzernamens.
     *
     * @param username der gesuchte Benutzername
     * @return der Benutzer oder ein leeres {@link Optional}, falls nicht
     *         vorhanden
     */
    Optional<User> findByUsername(String username);
}
