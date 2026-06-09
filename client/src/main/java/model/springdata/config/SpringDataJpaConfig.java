package model.springdata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring-Konfiguration für den Datenzugriff über Spring Data JPA.
 * <p>
 * Aktiviert die Auditing-Funktion (automatische Zeitstempel), die
 * Transaktionsverwaltung und die Repositories im Paket
 * {@code model.springdata.repository}.
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "model.springdata.repository")
public class SpringDataJpaConfig {
}
