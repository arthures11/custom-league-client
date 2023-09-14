package com.hawolt.ui.generic.utility;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.util.paint.PaintHelper;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 15/08/2023 21:25
 * Author: Twitter @hawolt
 **/

public class LazyLoadedImageComponent extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {
    protected final Dimension dimension;

    protected BufferedImage image;

    protected int x, y;

    public LazyLoadedImageComponent(Dimension dimension) {
        this(null, dimension);
    }

    public LazyLoadedImageComponent(Dimension dimension, int border) {
        this(null, dimension, border);
    }

    public LazyLoadedImageComponent(String uri, Dimension dimension) {
        this(uri, dimension, 0);
        ColorPalette.addThemeListener(this);
    }

    public LazyLoadedImageComponent(String uri, Dimension dimension, int border) {
        this.setPreferredSize(new Dimension(dimension.width + (border << 1), dimension.height + (border << 1)));
        this.dimension = dimension;
        if (uri == null) return;
        ResourceLoader.loadResource(uri, this);
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage bufferedImage) {
        this.image = Scalr.resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, dimension.width, dimension.height);
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;
        Dimension bounds = getSize();
        this.x = (bounds.width >> 1) - (image.getWidth() >> 1);
        this.y = (bounds.height >> 1) - (image.getHeight() >> 1);
        g.drawImage(PaintHelper.circleize(image, ColorPalette.CARD_ROUNDING), x, y, null);
    }
}
