package com.hawolt.ui.store;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.util.paint.PaintHelper;
import org.imgscalr.Scalr;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 09/08/2023 20:09
 * Author: Twitter @hawolt
 **/

public class StoreImage extends JComponent implements IStoreImage, ResourceConsumer<BufferedImage, byte[]> {
    private final StoreItem item;
    private BufferedImage image;

    public StoreImage(StoreItem item) {
        this.item = item;
    }

    @Override
    public String getImageURL(InventoryType type, long itemId) {
        switch (type) {
            case CHAMPION_SKIN -> {
                JSONObject raw = item.asJSON();
                JSONArray requirements = raw.getJSONArray("itemRequirements");
                Map<String, Long> map = new HashMap<>();
                for (int i = 0; i < requirements.length(); i++) {
                    JSONObject requirement = requirements.getJSONObject(i);
                    map.put(requirement.getString("inventoryType"), requirement.getLong("itemId"));
                }
                long skinId = map.getOrDefault(InventoryType.CHAMPION_SKIN.name(), itemId);
                if (item.hasSubInventoryType()) {
                    skinId = item.getItemId();
                    return String.format(
                            "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-chroma-images/%s/%s.png",
                            map.get(InventoryType.CHAMPION.name()),
                            skinId
                    );
                }
                return String.format(
                        "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg",
                        map.get(InventoryType.CHAMPION.name()),
                        skinId
                );
            }
            default -> {
                return String.format(
                        "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg",
                        item.getItemId(),
                        item.getItemId() * 1000
                );
            }
        }
    }

    public void load() {
        if (image != null) return;
        ResourceLoader.loadResource(getImageURL(item.getInventoryType(), item.getItemId()), this);
    }

    public void unload() {
        this.image = null;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Dimension dimension = getSize();
            int x = (dimension.width >> 1) - (image.getWidth() >> 1);
            int y = (dimension.height >> 1) - (image.getHeight() >> 1);
            g.drawImage(PaintHelper.circleize(image, ColorPalette.CARD_ROUNDING, x, y, dimension.width, dimension.height), 0, 0, null);
            if (!item.hasDiscount()) return;
            paintDiscountLabel(g);
        }
    }

    private void paintDiscountLabel(Graphics g) {
        if (item.getDiscountBE() > 0 || item.getDiscountRP() > 0) {
            Font font = new Font("Arial", Font.BOLD, 15);
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(font);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.RED.darker());
            g2.fillOval(3, 3, 42, 42);
            g2.setColor(Color.BLACK);
            g2.drawOval(2, 2, 44, 44);
            g2.drawOval(6, 6, 36, 36);
            g2.setColor(new Color(179, 140, 69));
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(4, 4, 40, 40);
            g2.setColor(Color.WHITE);
            if (item.hasDiscountBE()) {
                g2.drawString("-" + Math.round(item.getDiscountBE() * 100) + "%", 7, 30);
            } else {
                g2.drawString("-" + Math.round(item.getDiscountRP() * 100) + "%", 7, 30);
            }
        }
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load resource {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage image) {
        this.image = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, 200, 260);
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
