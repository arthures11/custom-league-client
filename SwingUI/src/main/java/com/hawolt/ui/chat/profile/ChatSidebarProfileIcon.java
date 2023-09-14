package com.hawolt.ui.chat.profile;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.paint.PaintHelper;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 08/08/2023 17:28
 * Author: Twitter @hawolt
 **/

public class ChatSidebarProfileIcon extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {
    private final String ICON_BASE_URL = "https://raw.communitydragon.org/latest/game/assets/ux/summonericons/profileicon%s.png";

    private final Color UNOBTAINED = new Color(231, 97, 97);
    private final Color GAINED = new Color(93, 156, 89);

    private final Font font = new Font("Arial", Font.BOLD, 16);
    private final int ICON_SIZE = 70;
    private int current, total, level;
    private BufferedImage icon;

    public ChatSidebarProfileIcon(UserInformation information, LayoutManager layout) {
        super(layout);
        this.setBackground(ColorPalette.accentColor);
        this.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        if (!information.isLeagueAccountAssociated()) return;
        this.current = 419;
        this.total = 2193;
        this.level = (int) information.getUserInformationLeagueAccount().getSummonerLevel();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //g.setColor(ColorPalette.cardColor);
        //g.fillRect(0,0,getWidth(),getHeight());

        Dimension dimension = getSize();
        g.setColor(UNOBTAINED);
        g.fillRect(0, ICON_SIZE, dimension.width, getHeight() - ICON_SIZE);
        double progress = ((double) current / (double) total);
        int width = (int) Math.floor(progress * (dimension.width - 1));
        g.setColor(GAINED);
        g.fillRect(0, ICON_SIZE, width, getHeight() - ICON_SIZE);

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(font);
        FontMetrics metrics = g.getFontMetrics();

        int y = (dimension.height >> 1) + (metrics.getAscent() >> 1);


        int levelStringWidth = metrics.stringWidth(String.valueOf(level));
        //drawHighlightedText(g, dimension, String.valueOf(level), dimension.width - 7 - levelStringWidth, y);
        if (icon == null) return;
        g.drawImage(PaintHelper.circleize(icon, ColorPalette.CARD_ROUNDING, true, true, false, false), 0, 0, null);
    }

    public void setIconId(long iconId) {
        ResourceLoader.loadResource(String.format(ICON_BASE_URL, iconId), this);
    }

    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load resource {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage image) {
        this.icon = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, ICON_SIZE, ICON_SIZE);
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
