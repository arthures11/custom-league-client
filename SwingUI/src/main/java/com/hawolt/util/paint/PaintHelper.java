package com.hawolt.util.paint;

import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Created: 15/08/2023 17:12
 * Author: Twitter @hawolt
 **/

public class PaintHelper {
    private static Font font;

    public static Graphics2D getGraphics2D() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        if (font == null) font = graphics2D.getFont();
        return graphics2D;
    }

    public static Font getFont() {
        if (font == null) getGraphics2D().dispose();
        return font;
    }

    public static void drawText(Graphics g, Font font, String text, int x, int y, Color textColor) {
        Color currentColor = g.getColor();
        Font currentFont = g.getFont();
        g.setFont(font);
        g.setColor(textColor);
        g.drawString(text, x, y);
        g.setFont(currentFont);
        g.setColor(currentColor);
    }

    public static void drawText(Graphics g, String text, int x, int y, Color textColor) {
        drawText(g, g.getFont(), text, x, y, textColor);
    }

    public static void drawShadowText(Graphics g, String text, int x, int y, Color textColor) {
        drawText(g, text, x + 1, y + 1, Color.BLACK);
        drawText(g, text, x, y, textColor);
    }

    public static void drawText(Graphics g, Font font, String text, Rectangle rectangle, LTextAlign alignment, Color textColor) {
        FontMetrics metrics = g.getFontMetrics();
        int y = rectangle.y + (rectangle.height >> 1) + (metrics.getAscent() >> 1);
        int x = switch (alignment) {
            case LEFT -> rectangle.x + (rectangle.width / 20);
            case CENTER -> rectangle.x - (rectangle.width >> 1) - (metrics.stringWidth(text) >> 1);
            case RIGHT -> rectangle.x + (rectangle.width - metrics.stringWidth(text) - (rectangle.width / 20));
        };
        drawText(g, font, text, x, y, textColor);
    }

    public static void drawShadowText(Graphics g, Font font, String text, Rectangle rectangle, LTextAlign alignment, Color textColor) {
        drawText(g, font, text, new Rectangle(rectangle.x + 1, rectangle.y + 1, rectangle.width, rectangle.height), alignment, Color.BLACK);
        drawText(g, font, text, rectangle, alignment, textColor);
    }

    public static void drawShadowText(Graphics g, String text, Rectangle rectangle, LTextAlign alignment, Color textColor) {
        drawShadowText(g, g.getFont(), text, rectangle, alignment, textColor);
    }

    public static int getFontHeight(FontMetrics metrics) {
        return metrics.getAscent() - metrics.getDescent() - metrics.getLeading();
    }

    //https://stackoverflow.com/questions/7603400/how-to-make-a-rounded-corner-image-in-java
    public static BufferedImage circleize(BufferedImage image, int rounding) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h,
                ColorPalette.useRoundedCorners ? rounding : 0, ColorPalette.useRoundedCorners ? rounding : 0));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    public static BufferedImage circleize(BufferedImage image, int rounding, boolean tr, boolean tl, boolean br, boolean bl) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        /*g2.fill(new RoundRectangle2D.Float(0, 0, w, h,
                ColorPalette.useRoundedCorners ? rounding : 0, ColorPalette.useRoundedCorners ? rounding: 0));*/
        roundedSquare(g2, 0, 0, w, h, rounding, tr, tl, br, bl);

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    public static BufferedImage circleize(BufferedImage image, int rounding, int imageX, int imageY) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h,
                ColorPalette.useRoundedCorners ? rounding : 0, ColorPalette.useRoundedCorners ? rounding : 0));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(image, imageX, imageY, null);

        g2.dispose();

        return output;
    }

    public static BufferedImage circleize(BufferedImage image, int rounding, int imageX, int imageY, int imageWidth, int imageHeight) {
        BufferedImage output = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, imageWidth, imageHeight,
                ColorPalette.useRoundedCorners ? rounding : 0, ColorPalette.useRoundedCorners ? rounding : 0));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(image, imageX, imageY, null);

        g2.dispose();

        return output;
    }

    public static void roundedSquare(Graphics2D g, int x, int y, int width, int height, int rounding, boolean roundTopRight, boolean roundTopLeft, boolean roundBotRight, boolean roundBotLeft) {
        rounding = ColorPalette.useRoundedCorners ? rounding : 0;
        int roundX = Math.min(width, rounding);
        int roundY = Math.min(height, rounding);

        Area topLeftArea = new Area(new RoundRectangle2D.Double(x, y, width, height, roundX, roundY));
        topLeftArea.add(new Area(new Rectangle2D.Double(x + roundX / 2, y, width - roundX / 2, height)));
        topLeftArea.add(new Area(new Rectangle2D.Double(x, y + roundY / 2, width, height - roundY / 2)));

        Area topRightArea = new Area(new RoundRectangle2D.Double(x, y, width, height, roundX, roundY));
        topRightArea.add(new Area(new Rectangle2D.Double(x, y, width - roundX / 2, height)));
        topRightArea.add(new Area(new Rectangle2D.Double(x, y + roundY / 2, width, height - roundY / 2)));

        Area botLeftArea = new Area(new RoundRectangle2D.Double(x, y, width, height, roundX, roundY));
        botLeftArea.add(new Area(new Rectangle2D.Double(x + roundX / 2, y, width - roundX / 2, height)));
        botLeftArea.add(new Area(new Rectangle2D.Double(x, y, width, height - roundY / 2)));

        Area botRightArea = new Area(new RoundRectangle2D.Double(x, y, width, height, roundX, roundY));
        botRightArea.add(new Area(new Rectangle2D.Double(x, y, width - roundX / 2, height)));
        botRightArea.add(new Area(new Rectangle2D.Double(x, y, width, height - roundY / 2)));

        Area paintArea = new Area(new Rectangle2D.Double(x, y, width, height));
        if (roundTopLeft)
            paintArea.intersect(topLeftArea);
        if (roundTopRight)
            paintArea.intersect(topRightArea);
        if (roundBotLeft)
            paintArea.intersect(botLeftArea);
        if (roundBotRight)
            paintArea.intersect(botRightArea);
        g.fill(paintArea);
    }
}
