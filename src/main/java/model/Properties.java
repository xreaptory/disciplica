package model;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Properties {
    public static Color MouseSelectedColor=Color.BLUEVIOLET;
    public static Color FocusOnComponentColor=Color.BLUE;
    public static Color ButtonBackgroundColor=Color.web("rgb(179,179,179)");
    public static Font sevenSegmentFont;

    public static Color ERMBackgroundColor = Color.GRAY;

    // images
    public static Image applicationImageIconAsICO;

    // statischer Initialisierungsblock wird beim Laden der Klasse ausgeführt
    // sinnvoll wenn die Resource immer geladen werden soll
    // wenn man selbst kontrollieren möchte, kann man über einen Konstruktor steuern
    static {
        applicationImageIconAsICO = new Image(Properties.class.getResource("/images/erdlogo.png").toExternalForm());
        sevenSegmentFont = Font.loadFont(Properties.class.getResourceAsStream("/fonts/DSEG7Classic-Regular.ttf"), 15);
    }

    /*
    public Properties(){
        applicationImageIconAsICO = new Image(getClass().getResource("/images/erdlogo.png").toExternalForm());
        // load used Font
        sevenSegmentFont = Font.loadFont(getClass().getResourceAsStream("/fonts/DSEG7Classic-Regular.ttf"), 15);
    }
    */

}
