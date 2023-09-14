package com.hawolt.ui.layout.wallet;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 09/08/2023 16:01
 * Author: Twitter @hawolt
 **/

public class HeaderWalletCurrency extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {
    private final Font font = new Font("Arial", Font.BOLD, 18);
    private BufferedImage image;
    private int amount;

    public HeaderWalletCurrency(String resource) {
        ColorPalette.addThemeListener(this);
        this.setBackground(ColorPalette.backgroundColor);
        ResourceLoader.loadResource(resource, this);
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;
        Dimension dimension = getSize();
        g.drawImage(image, 9, (dimension.height >> 1) - (image.getHeight() >> 1), null);

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setFont(font);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics metrics = g.getFontMetrics();

        int currencyWidth = metrics.stringWidth(String.valueOf(amount));
        int y = (dimension.height >> 1) + (metrics.getAscent() >> 1);
        int offset = 30;
        drawHighlightedText(g, String.valueOf(amount), offset + ((dimension.width - offset) >> 1) - (currencyWidth >> 1), y);
    }

    private void drawHighlightedText(Graphics g, String text, int x, int y) {
        g.setColor(Color.BLACK);
        g.drawString(text, x + 1, y + 1);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    public void deduct(int amount) {
        this.amount += amount;
        this.repaint();
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load image {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage bufferedImage) {
        this.image = Scalr.resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, 32, 32);
        this.repaint();
        this.revalidate();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
