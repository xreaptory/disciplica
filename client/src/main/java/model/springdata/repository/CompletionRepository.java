package model.springdata.repository;

import model.domain.model.Completion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionRepository extends JpaRepository<Completion, Long> {
}
