package View;

import java.util.prefs.Preferences;

import javafx.scene.Scene;

/**
 * Verwaltet das Erscheinungsbild (Theme) der Anwendung.
 * <p>
 * Jedes Theme besteht aus der gemeinsamen Basis-Stilvorlage
 * {@code habitica-theme.css} und – außer beim klassischen Theme – einer
 * zusätzlichen Datei, die nur die Farbvariablen und die Schriftart
 * überschreibt. Die Auswahl wird über {@link Preferences} dauerhaft
 * gespeichert und gilt damit auch nach einem Neustart.
 */
public final class ThemeManager {

    /**
     * Die verfügbaren Erscheinungsbilder.
     */
    public enum Theme {
        /** Klassisches lila Erscheinungsbild (nur Basis-Stilvorlage). */
        CLASSIC("Classic", null),
        /** Echtes Dunkeldesign (nahezu schwarz) mit blauem Akzent. */
        BLACK("Black", "/css/theme-black.css"),
        /** Helles Design mit dunkler Schrift. */
        LIGHT("Light", "/css/theme-light.css");

        private final String label;
        private final String resource;

        Theme(String label, String resource) {
            this.label = label;
            this.resource = resource;
        }

        /**
         * {@return der Anzeigename des Themes}
         */
        public String label() {
            return label;
        }

        /**
         * {@return der Pfad zur zusätzlichen Stildatei oder {@code null}, wenn
         * nur die Basis-Stilvorlage benötigt wird}
         */
        public String resource() {
            return resource;
        }

        /**
         * Ermittelt ein Theme anhand seines Anzeigenamens.
         *
         * @param label der Anzeigename
         * @return das passende Theme oder {@link #CLASSIC}, falls keines passt
         */
        public static Theme fromLabel(String label) {
            for (Theme theme : values()) {
                if (theme.label.equals(label)) {
                    return theme;
                }
            }
            return CLASSIC;
        }
    }

    private static final String BASE_STYLESHEET = "/css/habitica-theme.css";
    private static final Preferences PREFS = Preferences.userRoot().node("disciplica/ui");
    private static final String THEME_KEY = "theme";

    private ThemeManager() {
    }

    /**
     * {@return das aktuell gespeicherte Theme (Standard: {@link Theme#CLASSIC})}
     */
    public static Theme current() {
        return Theme.fromLabel(PREFS.get(THEME_KEY, Theme.CLASSIC.label()));
    }

    /**
     * Speichert das gewählte Theme dauerhaft.
     *
     * @param theme das zu speichernde Theme
     */
    public static void setCurrent(Theme theme) {
        PREFS.put(THEME_KEY, theme.label());
    }

    /**
     * Wendet das aktuell gespeicherte Theme auf eine Szene an.
     *
     * @param scene die zu gestaltende Szene
     */
    public static void apply(Scene scene) {
        apply(scene, current());
    }

    /**
     * Wendet ein bestimmtes Theme auf eine Szene an (Basis-Stilvorlage plus
     * optionaler Theme-Datei).
     *
     * @param scene die zu gestaltende Szene
     * @param theme das anzuwendende Theme
     */
    public static void apply(Scene scene, Theme theme) {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().clear();
        addStylesheet(scene, BASE_STYLESHEET);
        if (theme.resource() != null) {
            addStylesheet(scene, theme.resource());
        }
    }

    /**
     * Fügt eine Stildatei hinzu, sofern sie im Klassenpfad gefunden wird.
     *
     * @param scene    die Szene
     * @param resource der Pfad der Stildatei
     */
    private static void addStylesheet(Scene scene, String resource) {
        var url = ThemeManager.class.getResource(resource);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        }
    }
}
