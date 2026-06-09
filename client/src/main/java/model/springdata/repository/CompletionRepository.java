package model.springdata.repository;

import model.domain.model.Completion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring-Data-Repository für {@link Completion Erfüllungen}.
 * <p>
 * Stellt durch das Erben von {@link JpaRepository} die üblichen
 * CRUD-Operationen bereit.
 */
public interface CompletionRepository extends JpaRepository<Completion, Long> {
}
