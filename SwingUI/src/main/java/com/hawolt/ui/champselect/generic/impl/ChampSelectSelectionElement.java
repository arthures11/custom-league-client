package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.logger.Logger;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.paint.PaintHelper;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 29/08/2023 22:00
 * Author: Twitter @hawolt
 **/

public class ChampSelectSelectionElement extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {

    private static final Dimension IMAGE_TARGET_DIMENSION = new Dimension(72, 72);
    private static final Font FONT = new Font("Arial", Font.PLAIN, 18);
    private final ChampSelectType type;
    private final int championId;
    private final String name;

    private BufferedImage image;
    private boolean selected;

    public ChampSelectSelectionElement(ChampSelectChoice callback, ChampSelectType type, int championId, String name) {
        super(new BorderLayout());
        this.name = name;
        this.type = type;
        this.championId = championId;
        this.addMouseListener(new ChampSelectSelectionElementAdapter(callback));
        this.setPreferredSize(new Dimension(80, 100));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dimension = getSize();
        int imageX = (dimension.width >> 1) - (IMAGE_TARGET_DIMENSION.width >> 1);
        if (image != null) {
            g.drawImage(PaintHelper.circleize(image, ColorPalette.CARD_ROUNDING), imageX, 0, null);
        }
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setFont(FONT);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics metrics = graphics2D.getFontMetrics();
        int textWidth = metrics.stringWidth(name);
        if (textWidth <= dimension.width) {
            graphics2D.setColor(ColorPalette.textColor);
            int textX = (dimension.width >> 1) - (textWidth >> 1);
            int textY = 15 + (metrics.getAscent() >> 1);
            graphics2D.drawString(name, textX, IMAGE_TARGET_DIMENSION.height + textY);
        }
        if (selected) {
            Color color = type == ChampSelectType.PICK ? Color.CYAN : Color.RED;
            Color infused = new Color((color.getRGB() & 0xFFFFFF) | (0x7F << 24), true);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(infused);
            graphics2D.fillRoundRect(imageX, 0, IMAGE_TARGET_DIMENSION.width, IMAGE_TARGET_DIMENSION.height,
                    ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0, ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0);
        }
    }

    public void setSelected(boolean b) {
        this.selected = b;
    }

    public int getChampionId() {
        return championId;
    }

    @Override
    public String getName() {
        return name;
    }

    public ChampSelectType getType() {
        return type;
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.warn("Failed to load {}");
    }

    @Override
    public void consume(Object o, BufferedImage bufferedImage) {
        this.image = Scalr.resize(
                bufferedImage,
                Scalr.Method.ULTRA_QUALITY,
                Scalr.Mode.FIT_TO_HEIGHT,
                IMAGE_TARGET_DIMENSION.width,
                IMAGE_TARGET_DIMENSION.height
        );
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    private class ChampSelectSelectionElementAdapter extends MouseAdapter {
        private final ChampSelectChoice callback;

        public ChampSelectSelectionElementAdapter(ChampSelectChoice callback) {
            this.callback = callback;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            try {
                Logger.info("[champ-select] indicate {}", championId);
                Dimension dimension = getSize();
                int rectangleX = (dimension.width >> 1) - (IMAGE_TARGET_DIMENSION.width >> 1);
                Rectangle rectangle = new Rectangle(rectangleX, 0, IMAGE_TARGET_DIMENSION.width, IMAGE_TARGET_DIMENSION.height);
                selected = rectangle.contains(e.getPoint());
                if (!selected) return;
                Logger.info("[champ-select] forward {} as indicator", championId);
                ChampSelectSelectionElement.this.repaint();
                callback.onChoice(ChampSelectSelectionElement.this);
            } catch (Exception ex) {
                Logger.error(ex);
            }
        }
    }

}
