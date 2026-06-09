package model.domain.contract;

import model.domain.model.Reward;

/**
 * Schnittstelle für alles, was abgeschlossen werden kann und dafür eine
 * Belohnung gewährt (z.&nbsp;B. Aufgaben oder Gewohnheiten).
 */
public interface Completable {

    /**
     * Schließt das Element ab.
     *
     * @return {@code true}, wenn der Abschluss erfolgreich war
     */
    boolean complete();

    /**
     * Gibt die Belohnung zurück, die beim Abschließen gewährt wird.
     *
     * @return die zugehörige Belohnung
     */
    Reward getReward();
}
