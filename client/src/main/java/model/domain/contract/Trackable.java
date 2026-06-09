package model.domain.contract;

/**
 * Schnittstelle für alles, dessen Fortschritt verfolgt werden kann
 * (z.&nbsp;B. Gewohnheiten mit Fortschritt und Serie).
 */
public interface Trackable {

    /**
     * Gibt den Namen des verfolgten Elements zurück.
     *
     * @return der Name
     */
    String getName();

    /**
     * Gibt den aktuellen Fortschritt zurück.
     *
     * @return der Fortschritt
     */
    int getProgress();

    /**
     * Gibt die aktuelle Serie (aufeinanderfolgende Erfüllungen) zurück.
     *
     * @return die aktuelle Serie
     */
    int getStreak();
}
