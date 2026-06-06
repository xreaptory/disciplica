package model.springdata.repository;

import model.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSpringRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
