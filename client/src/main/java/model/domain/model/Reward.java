package model.domain.model;

import model.domain.contract.Trackable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eine Belohnung, die der Benutzer mit gesammelten Punkten erreichen bzw.
 * „kaufen“ kann.
 */
public class Reward implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(Reward.class);
    private String name;
    private String description;
    private int pointsRequired;

    /**
     * Erzeugt eine neue Belohnung.
     *
     * @param name           der Name der Belohnung
     * @param description    die Beschreibung der Belohnung
     * @param pointsRequired die für die Belohnung benötigten Punkte
     */
    public Reward(String name, String description, int pointsRequired) {
        this.name = name;
        this.description = description;
        this.pointsRequired = pointsRequired;
        logger.debug("Reward created: name='{}', pointsRequired={}", name, pointsRequired);
    }

    /**
     * {@return der Name der Belohnung}
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt den Namen der Belohnung.
     *
     * @param name der neue Name
     */
    public void setName(String name) {
        logger.debug("Reward name changed from '{}' to '{}'", this.name, name);
        this.name = name;
    }

    /**
     * {@return die Beschreibung der Belohnung}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setzt die Beschreibung der Belohnung.
     *
     * @param description die neue Beschreibung
     */
    public void setDescription(String description) {
        logger.debug("Reward '{}' description updated", this.name);
        this.description = description;
    }

    /**
     * {@return die für die Belohnung benötigten Punkte}
     */
    public int getPointsRequired() {
        return pointsRequired;
    }

    /**
     * Setzt die für die Belohnung benötigten Punkte.
     *
     * @param pointsRequired die neue Punkteanforderung
     */
    public void setPointsRequired(int pointsRequired) {
        logger.debug("Reward '{}' pointsRequired changed from {} to {}", name, this.pointsRequired, pointsRequired);
        this.pointsRequired = pointsRequired;
    }

    /**
     * Eine Belohnung hat keinen eigenen Fortschritt; gibt immer 0 zurück.
     *
     * @return immer 0
     */
    @Override
    public int getProgress() {
        return 0;
    }

    /**
     * Eine Belohnung führt keine Serie; gibt immer 0 zurück.
     *
     * @return immer 0
     */
    @Override
    public int getStreak() {
        return 0;
    }

    /**
     * {@return eine textuelle Darstellung der Belohnung mit Name, Punkten und
     * Beschreibung}
     */
    @Override
    public String toString() {
        return name + " (" + pointsRequired + " pts) - " + description;
    }
}
