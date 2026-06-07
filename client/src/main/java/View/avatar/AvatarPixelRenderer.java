package View.avatar;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public final class AvatarPixelRenderer {
    private AvatarPixelRenderer() {
    }

    public static void render(Canvas canvas, AvatarState state) {
        render(canvas, state, Equipment.none());
    }

    public static void render(Canvas canvas, AvatarState state, Equipment equipment) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        gc.clearRect(0, 0, width, height);
        gc.setImageSmoothing(false);

        int pixel = Math.max(5, (int) Math.round(Math.min(width / 54.0, height / 36.0)));
        int originX = (int) Math.round(width / 2.0 - 13 * pixel);
        int originY = (int) Math.round(height / 2.0 - 15 * pixel);

        drawStage(gc, width, height);

        Color outline = Color.web("#201738");
        Color skin = skinColor(state.getSkinColor());
        Color skinShade = shade(skin, 0.78);
        Color shirt = shirtColor(state.getShirtColor());
        Color shirtShade = shade(shirt, 0.76);
        Color hair = hairColor(state.getHairColor());

        int bodyHeight = switch (state.getBodySize()) {
            case "Small" -> 8;
            case "Tall" -> 12;
            default -> 10;
        };
        int bodyTop = 15 - Math.max(0, bodyHeight - 10);
        int headTop = bodyTop - 7;
        int headLeft = 9;
        int torsoLeft = 8;

        if ("Cape".equals(equipment.armor())) {
            rect(gc, originX, originY, 6, bodyTop - 1, 16, bodyHeight + 5, pixel, Color.web("#6a49c9"));
            rect(gc, originX, originY, 8, bodyTop, 12, bodyHeight + 5, pixel, Color.web("#52359f"));
        }

        if ("Wheelchair".equals(state.getExtra())) {
            drawWheelchair(gc, originX, originY, pixel, torsoLeft, bodyTop, bodyHeight, outline);
        }

        drawBackHair(gc, originX, originY, pixel, state, hair, headLeft, headTop);
        rect(gc, originX, originY, headLeft - 1, headTop - 1, 10, 9, pixel, outline);
        rect(gc, originX, originY, headLeft, headTop, 8, 7, pixel, skin);
        rect(gc, originX, originY, headLeft, headTop + 6, 8, 1, pixel, skinShade);
        drawFrontHair(gc, originX, originY, pixel, state, hair, headLeft, headTop);

        rect(gc, originX, originY, headLeft + 2, headTop + 3, 1, 1, pixel, Color.web("#1a1430"));
        rect(gc, originX, originY, headLeft + 6, headTop + 3, 1, 1, pixel, Color.web("#1a1430"));
        rect(gc, originX, originY, headLeft + 4, headTop + 5, 2, 1, pixel, Color.web("#8b3f3f"));

        rect(gc, originX, originY, torsoLeft - 1, bodyTop - 1, 12, bodyHeight + 2, pixel, outline);
        rect(gc, originX, originY, torsoLeft, bodyTop, 10, bodyHeight, pixel, shirt);
        rect(gc, originX, originY, torsoLeft + 2, bodyTop + 2, 6, Math.max(2, bodyHeight - 3), pixel, shirtShade);

        drawArm(gc, originX, originY, pixel, torsoLeft - 4, bodyTop + 1, skin, shirt, outline, false);
        drawArm(gc, originX, originY, pixel, torsoLeft + 10, bodyTop + 1, skin, shirt, outline, true);

        rect(gc, originX, originY, torsoLeft + 1, bodyTop + bodyHeight, 3, 7, pixel, Color.web("#2d347e"));
        rect(gc, originX, originY, torsoLeft + 6, bodyTop + bodyHeight, 3, 7, pixel, Color.web("#2d347e"));
        rect(gc, originX, originY, torsoLeft + 1, bodyTop + bodyHeight + 7, 3, 1, pixel, outline);
        rect(gc, originX, originY, torsoLeft + 6, bodyTop + bodyHeight + 7, 3, 1, pixel, outline);

        drawExtra(gc, originX, originY, pixel, state.getExtra(), headLeft, headTop);
        drawEquipment(gc, originX, originY, pixel, equipment, torsoLeft, bodyTop, bodyHeight, skin);
    }

    private static void drawStage(GraphicsContext gc, double width, double height) {
        gc.setFill(Color.web("#d8ecff"));
        gc.fillRect(0, 0, width, height);
        gc.setFill(Color.web("#acd993"));
        gc.fillOval(width * 0.16, height * 0.65, width * 0.68, height * 0.2);
        gc.setFill(Color.web("#f7fbff", 0.62));
        gc.fillRect(width * 0.18, height * 0.15, width * 0.18, height * 0.05);
        gc.fillRect(width * 0.54, height * 0.2, width * 0.2, height * 0.05);
    }

    private static void drawBackHair(GraphicsContext gc, int ox, int oy, int p, AvatarState state, Color hair, int x, int y) {
        if ("Long".equals(state.getHairStyle())) {
            rect(gc, ox, oy, x - 2, y - 2, 12, 12, p, Color.web("#201738"));
            rect(gc, ox, oy, x - 1, y - 1, 10, 12, p, hair);
            rect(gc, ox, oy, x, y + 7, 8, 4, p, shade(hair, 0.72));
        }
    }

    private static void drawFrontHair(GraphicsContext gc, int ox, int oy, int p, AvatarState state, Color hair, int x, int y) {
        String style = state.getHairStyle();
        if ("Spikes".equals(style)) {
            rect(gc, ox, oy, x - 1, y - 2, 10, 2, p, hair);
            triangle(gc, ox, oy, x, y - 5, x + 2, y - 1, x - 1, y - 1, p, hair);
            triangle(gc, ox, oy, x + 4, y - 6, x + 6, y - 1, x + 2, y - 1, p, hair);
            triangle(gc, ox, oy, x + 8, y - 5, x + 9, y - 1, x + 6, y - 1, p, hair);
        } else if ("Bangs".equals(style) || "Bangs".equals(state.getHairBangs())) {
            rect(gc, ox, oy, x - 1, y - 2, 10, 3, p, hair);
            rect(gc, ox, oy, x + 1, y, 2, 2, p, shade(hair, 1.18));
            rect(gc, ox, oy, x + 4, y, 3, 2, p, shade(hair, 0.88));
        } else {
            rect(gc, ox, oy, x - 1, y - 2, 10, 3, p, hair);
            rect(gc, ox, oy, x + 1, y - 3, 6, 1, p, shade(hair, 1.14));
        }
    }

    private static void drawArm(GraphicsContext gc, int ox, int oy, int p, int x, int y,
                                Color skin, Color shirt, Color outline, boolean right) {
        rect(gc, ox, oy, x, y, 4, 8, p, outline);
        rect(gc, ox, oy, x + 1, y + 1, 3, 5, p, shirt);
        rect(gc, ox, oy, x + (right ? 2 : 1), y + 6, 2, 2, p, skin);
    }

    private static void drawExtra(GraphicsContext gc, int ox, int oy, int p, String extra, int headLeft, int headTop) {
        if ("Glasses".equals(extra)) {
            rect(gc, ox, oy, headLeft + 1, headTop + 2, 3, 2, p, Color.web("#51466f"));
            rect(gc, ox, oy, headLeft + 5, headTop + 2, 3, 2, p, Color.web("#51466f"));
            rect(gc, ox, oy, headLeft + 4, headTop + 3, 1, 1, p, Color.web("#51466f"));
        }
    }

    private static void drawWheelchair(GraphicsContext gc, int ox, int oy, int p,
                                       int torsoLeft, int bodyTop, int bodyHeight, Color outline) {
        Color frame = Color.web("#6f7893");
        Color frameLight = Color.web("#aeb7c8");
        Color wheel = Color.web("#3a4058");
        Color wheelLight = Color.web("#d8deea");
        int seatY = bodyTop + bodyHeight - 2;
        int wheelY = bodyTop + bodyHeight + 3;

        rect(gc, ox, oy, torsoLeft - 5, bodyTop + 3, 2, bodyHeight + 4, p, outline);
        rect(gc, ox, oy, torsoLeft - 4, bodyTop + 4, 1, bodyHeight + 2, p, frameLight);
        rect(gc, ox, oy, torsoLeft + 11, bodyTop + 5, 2, bodyHeight + 1, p, outline);
        rect(gc, ox, oy, torsoLeft + 12, bodyTop + 6, 1, bodyHeight - 1, p, frameLight);

        rect(gc, ox, oy, torsoLeft - 4, seatY, 17, 2, p, outline);
        rect(gc, ox, oy, torsoLeft - 3, seatY, 15, 1, p, frame);
        rect(gc, ox, oy, torsoLeft - 2, seatY + 2, 12, 2, p, frame);

        drawWheel(gc, ox, oy, p, torsoLeft - 6, wheelY, wheel, wheelLight, outline);
        drawWheel(gc, ox, oy, p, torsoLeft + 10, wheelY, wheel, wheelLight, outline);

        rect(gc, ox, oy, torsoLeft + 8, seatY + 3, 7, 1, p, outline);
        rect(gc, ox, oy, torsoLeft + 13, seatY + 2, 1, 5, p, frame);
        rect(gc, ox, oy, torsoLeft + 14, seatY + 6, 3, 1, p, frame);
    }

    private static void drawWheel(GraphicsContext gc, int ox, int oy, int p, int x, int y,
                                  Color wheel, Color wheelLight, Color outline) {
        rect(gc, ox, oy, x + 1, y, 5, 1, p, outline);
        rect(gc, ox, oy, x, y + 1, 7, 5, p, outline);
        rect(gc, ox, oy, x + 1, y + 2, 5, 3, p, wheel);
        rect(gc, ox, oy, x + 3, y + 3, 1, 1, p, wheelLight);
        rect(gc, ox, oy, x + 1, y + 6, 5, 1, p, outline);
    }

    private static void drawEquipment(GraphicsContext gc, int ox, int oy, int p, Equipment equipment,
                                      int torsoLeft, int bodyTop, int bodyHeight, Color skin) {
        if ("Armor".equals(equipment.armor())) {
            rect(gc, ox, oy, torsoLeft, bodyTop + 1, 10, Math.max(5, bodyHeight - 1), p, Color.web("#8da6bd"));
            rect(gc, ox, oy, torsoLeft + 3, bodyTop + 2, 4, Math.max(3, bodyHeight - 3), p, Color.web("#5f7388"));
        } else if ("DragonArmor".equals(equipment.armor())) {
            rect(gc, ox, oy, torsoLeft, bodyTop + 1, 10, Math.max(5, bodyHeight - 1), p, Color.web("#583481"));
            rect(gc, ox, oy, torsoLeft + 3, bodyTop + 2, 4, Math.max(3, bodyHeight - 3), p, Color.web("#7e4ab1"));
        }

        if ("Crown".equals(equipment.headgear()) || "RoyalCrown".equals(equipment.headgear())) {
            Color crown = "RoyalCrown".equals(equipment.headgear()) ? Color.web("#ffe07c") : Color.web("#ffd45c");
            rect(gc, ox, oy, 8, bodyTop - 9, 10, 2, p, crown);
            rect(gc, ox, oy, 9, bodyTop - 10, 2, 1, p, crown);
            rect(gc, ox, oy, 12, bodyTop - 11, 2, 2, p, crown);
            rect(gc, ox, oy, 16, bodyTop - 10, 2, 1, p, crown);
        } else if ("Helm".equals(equipment.headgear())) {
            rect(gc, ox, oy, 8, bodyTop - 9, 10, 3, p, Color.web("#9ec4e6"));
            rect(gc, ox, oy, 9, bodyTop - 6, 8, 1, p, Color.web("#6e8ca8"));
        }

        int handX = torsoLeft + 13;
        int handY = bodyTop + 8;
        if ("Sword".equals(equipment.weapon()) || "Greatsword".equals(equipment.weapon())) {
            int bladeHeight = "Greatsword".equals(equipment.weapon()) ? 13 : 9;
            rect(gc, ox, oy, handX + 2, handY - bladeHeight, 2, bladeHeight, p, Color.web("#dfe7ef"));
            rect(gc, ox, oy, handX, handY - 1, 6, 1, p, Color.web("#ffd45c"));
            rect(gc, ox, oy, handX + 2, handY, 2, 3, p, Color.web("#8b5a2b"));
        } else if ("Axe".equals(equipment.weapon()) || "Halberd".equals(equipment.weapon())) {
            int shaftHeight = "Halberd".equals(equipment.weapon()) ? 15 : 10;
            rect(gc, ox, oy, handX + 2, handY - shaftHeight, 2, shaftHeight + 4, p, Color.web("#8b5a2b"));
            rect(gc, ox, oy, handX, handY - shaftHeight, 5, 3, p, Color.web("#a7b4c6"));
        }
        rect(gc, ox, oy, handX, handY, 2, 2, p, skin);
    }

    private static Color skinColor(String value) {
        return switch (value) {
            case "Tan" -> Color.web("#d79055");
            case "Brown" -> Color.web("#8b5a37");
            case "Fantasy" -> Color.web("#82d8ce");
            case "Dark" -> Color.web("#70452d");
            default -> Color.web("#f2c8a0");
        };
    }

    private static Color hairColor(String value) {
        return switch (value) {
            case "Black" -> Color.web("#20242a");
            case "Blonde" -> Color.web("#d8bf64");
            case "Red" -> Color.web("#c95d28");
            case "White" -> Color.web("#d8d8e8");
            default -> Color.web("#6d3f28");
        };
    }

    private static Color shirtColor(String value) {
        return switch (value) {
            case "Green" -> Color.web("#42b95a");
            case "Pink" -> Color.web("#f08fa2");
            case "Yellow" -> Color.web("#f4cf4f");
            case "Gray" -> Color.web("#a6a5b4");
            default -> Color.web("#5a83e6");
        };
    }

    private static Color shade(Color color, double factor) {
        return Color.color(
                clamp(color.getRed() * factor),
                clamp(color.getGreen() * factor),
                clamp(color.getBlue() * factor),
                color.getOpacity()
        );
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static void rect(GraphicsContext gc, int ox, int oy, int x, int y, int w, int h, int p, Color color) {
        gc.setFill(color);
        gc.fillRect(ox + x * p, oy + y * p, w * p, h * p);
    }

    private static void triangle(GraphicsContext gc, int ox, int oy, int x1, int y1, int x2, int y2, int x3, int y3, int p, Color color) {
        gc.setFill(color);
        gc.fillPolygon(
                new double[]{ox + x1 * p, ox + x2 * p, ox + x3 * p},
                new double[]{oy + y1 * p, oy + y2 * p, oy + y3 * p},
                3
        );
    }

    public record Equipment(String weapon, String armor, String headgear) {
        public static Equipment none() {
            return new Equipment("None", "None", "None");
        }
    }
}
