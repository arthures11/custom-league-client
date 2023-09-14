package com.hawolt.ui.store;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.purchasewidget.CurrencyType;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.utility.HighlightType;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 09/08/2023 20:13
 * Author: Twitter @hawolt
 **/

public class StoreButton extends LFlatButton implements MouseListener, MouseMotionListener, ResourceConsumer<BufferedImage, byte[]> {
    private final static String BASE = "https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images";
    private final IStoreElement element;
    private final CurrencyType currency;
    private final int price;
    private final Font font = new Font("Dialog", Font.PLAIN, 18);
    private BufferedImage image;

    public StoreButton(IStoreElement element, CurrencyType currency, int price) {
        setTextAlign(LTextAlign.CENTER);
        setHighlightType(HighlightType.COMPONENT);
        this.setPreferredSize(new Dimension(0, 30));
        this.setBackground(Color.DARK_GRAY);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.currency = currency;
        this.element = element;
        this.price = price;
        ResourceLoader.loadResource(String.join("/", BASE, currency == CurrencyType.IP ? "icon-be-150.png" : "icon-rp-72.png"), this);
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load image {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage bufferedImage) {
        this.image = Scalr.resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, 16, 16);
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public int getPrice() {
        return price;
    }

    @Override
    protected void paintComponent(Graphics g) {
        setText(String.valueOf(price));
        Dimension dimension = getSize();
        super.paintComponent(g);
        /*Dimension dimension = getSize();
        Graphics2D graphics2D = (Graphics2D) g;
        FontMetrics metrics = g.getFontMetrics();
        graphics2D.setFont(font);
        int width = metrics.stringWidth(String.valueOf(price));
        graphics2D.setColor(ColorPalette.textColor);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int offset = 21;
        int x = ((dimension.width - offset) >> 1) - (width >> 1);
        graphics2D.drawString(String.valueOf(price), offset + x, (dimension.height >> 1) + (metrics.getAscent() >> 1));*/
        if (image == null) return;
        g.drawImage(image, 5, (dimension.height >> 1) - (image.getHeight() >> 1), null);
    }


    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //this.setBackground(Color.LIGHT_GRAY);
        this.element.purchase(currency, price);
        this.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
