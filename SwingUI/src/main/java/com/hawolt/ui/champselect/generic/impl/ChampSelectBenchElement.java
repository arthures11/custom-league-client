package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.async.Debouncer;
import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created: 29/08/2023 22:00
 * Author: Twitter @hawolt
 **/

public class ChampSelectBenchElement extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {
    private static final String IMAGE_ICON_BASE = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-icons/%s.png";
    private static final Dimension IMAGE_TARGET_DIMENSION = new Dimension(64, 64);
    private int championId;
    private BufferedImage image;

    public ChampSelectBenchElement(ChampSelectChoice callback) {
        super(new BorderLayout());
        this.setPreferredSize(new Dimension(80, 80));
        ChampSelectSelectionElementAdapter adapter = new ChampSelectSelectionElementAdapter(callback);
        this.addMouseMotionListener(adapter);
        this.addMouseListener(adapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dimension = getSize();
        int imageX = (dimension.width >> 1) - (IMAGE_TARGET_DIMENSION.width >> 1);
        int imageY = (dimension.height >> 1) - (IMAGE_TARGET_DIMENSION.height >> 1);
        if (image != null) {
            g.setColor(Color.BLACK);
            g.fillRect(imageX - 1, imageY - 1, IMAGE_TARGET_DIMENSION.width + 2, IMAGE_TARGET_DIMENSION.height + 2);
            g.drawImage(image, imageX, imageY, null);
        }
    }

    public int getChampionId() {
        return championId;
    }

    public void setChampionId(int championId) {
        if (this.championId == championId) return;
        ResourceLoader.loadResource(String.format(IMAGE_ICON_BASE, this.championId = championId), this);
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
        private final Debouncer debouncer = new Debouncer();
        private final ChampSelectChoice callback;

        public ChampSelectSelectionElementAdapter(ChampSelectChoice callback) {
            this.callback = callback;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            try {
                if (championId == -1) return;
                Logger.info("[champ-select] swap {}", championId);
                Dimension dimension = getSize();
                int rectangleX = (dimension.width >> 1) - (IMAGE_TARGET_DIMENSION.width >> 1);
                Rectangle rectangle = new Rectangle(rectangleX, 0, IMAGE_TARGET_DIMENSION.width, IMAGE_TARGET_DIMENSION.height);
                if (!rectangle.contains(e.getPoint())) return;
                Logger.info("[champ-select] swap to champion", championId);
                ChampSelectBenchElement.this.repaint();
                callback.onSwapChoice(ChampSelectBenchElement.this);
            } catch (Exception ex) {
                Logger.error(ex);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (championId == -1) return;
            debouncer.debounce("hover", () -> {
                Dimension dimension = getSize();
                int rectangleX = (dimension.width >> 1) - (IMAGE_TARGET_DIMENSION.width >> 1);
                int imageY = (dimension.height >> 1) - (IMAGE_TARGET_DIMENSION.height >> 1);
                Rectangle rectangle = new Rectangle(rectangleX, imageY, IMAGE_TARGET_DIMENSION.width, IMAGE_TARGET_DIMENSION.height);
                if (rectangle.contains(e.getPoint())) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }, 10L, TimeUnit.MILLISECONDS);
        }
    }

}
