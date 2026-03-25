package model;

import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Tools
 *
 * @author Leo Fanzott
 */
public class SCTools {

    public static void ButtonMouseEntered(Node node, double yScale, double xScale, double contrast, double hue, double brightness, double saturation){
        ImageView iv=(ImageView)node;
        // scale image
        iv.setScaleY(yScale);
        iv.setScaleX(xScale);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setContrast(contrast);
        colorAdjust.setHue(hue);
        colorAdjust.setBrightness(brightness);
        colorAdjust.setSaturation(saturation);
        iv.setEffect(colorAdjust);
    }

    public static void ButtonMouseExited(Node node){
        // TODO Auto-generated method stub
        ImageView iv=(ImageView)node;
        // rescale to origin
        iv.setScaleX(1);
        iv.setScaleY(1);
        iv.setEffect(null);

    }

    public static void BorderToNode(Node node, Color color, int depth){
        DropShadow borderGlow= new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(color);
        borderGlow.setWidth(10);
        borderGlow.setHeight(10);
        node.setEffect(borderGlow);
    }

}
