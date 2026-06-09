package model;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.io.InputStream;
import java.net.URL;

/**
 * Zentrale Sammlung von Oberflächen-Konstanten (Farben, Schriftart und
 * Anwendungssymbol), die an mehreren Stellen der Benutzeroberfläche verwendet
 * werden.
 * <p>
 * Die Ressourcen (Symbol und Schriftart) werden einmalig beim Laden der
 * Klasse aus dem Klassenpfad geladen.
 */
public class Properties {
    /** Farbe zur Hervorhebung eines mit der Maus ausgewählten Elements. */
    public static Color MouseSelectedColor=Color.BLUEVIOLET;
    /** Farbe zur Hervorhebung des Elements, das den Eingabefokus besitzt. */
    public static Color FocusOnComponentColor=Color.BLUE;
    /** Hintergrundfarbe für Schaltflächen. */
    public static Color ButtonBackgroundColor=Color.web("rgb(179,179,179)");
    /** Sieben-Segment-Schriftart für zahlenartige Anzeigen. */
    public static Font sevenSegmentFont;

    /** Hintergrundfarbe des Hauptbereichs. */
    public static Color ERMBackgroundColor = Color.GRAY;

    /** Anwendungssymbol, das im Fenstertitel angezeigt wird. */
    public static Image applicationImageIconAsICO;

    // Statischer Initialisierungsblock: wird beim Laden der Klasse ausgeführt
    // und lädt die benötigten Ressourcen ein.
    static {
        URL iconUrl = Properties.class.getResource("/images/erdlogo.png");
        if (iconUrl != null) {
            applicationImageIconAsICO = new Image(iconUrl.toExternalForm());
        }

        InputStream fontStream = Properties.class.getResourceAsStream("/fonts/DSEG7Classic-Regular.ttf");
        if (fontStream != null) {
            Font loadedFont = Font.loadFont(fontStream, 15);
            sevenSegmentFont = loadedFont != null ? loadedFont : Font.font("System", 15);
        } else {
            sevenSegmentFont = Font.font("System", 15);
        }
    }

}
