package model.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;

/**
 * Hilfsklasse zum Verwalten der JPA-{@link EntityManagerFactory} und zum
 * Erzeugen von {@link EntityManager}n.
 * <p>
 * Hält die Factory als gemeinsame Instanz vor und stellt sicher, dass sie bei
 * Bedarf neu erzeugt wird.
 */
public final class JpaEntityManagerHelper {
    private static final String PERSISTENCE_UNIT_NAME = "disciplicaPU";
    private static EntityManagerFactory entityManagerFactory;

    private JpaEntityManagerHelper() {
    }

    /**
     * Gibt die gemeinsame {@link EntityManagerFactory} zurück und erzeugt sie
     * bei Bedarf.
     *
     * @return die einsatzbereite Factory
     */
    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return entityManagerFactory;
    }

    /**
     * Erzeugt die {@link EntityManagerFactory} mit zusätzlichen Einstellungen
     * neu (z.&nbsp;B. für Tests mit abweichender Datenbank).
     *
     * @param overrides die zusätzlichen bzw. überschreibenden Einstellungen
     * @return die neu erzeugte Factory
     */
    public static synchronized EntityManagerFactory createEntityManagerFactory(Map<String, Object> overrides) {
        close();
        entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, overrides);
        return entityManagerFactory;
    }

    /**
     * Erzeugt einen neuen {@link EntityManager}.
     *
     * @return ein neuer EntityManager
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Schließt die gemeinsame {@link EntityManagerFactory}, falls sie geöffnet
     * ist.
     */
    public static synchronized void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        entityManagerFactory = null;
    }
}
