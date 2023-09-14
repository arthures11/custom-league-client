package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.ui.champselect.data.ActionObject;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.LazyLoadedImageComponent;

import java.awt.*;

/**
 * Created: 31/08/2023 21:17
 * Author: Twitter @hawolt
 **/

public class ChampSelectBanElement extends LazyLoadedImageComponent {
    private final static String preset = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-icons/%s.png";
    private boolean completed;
    private int championId;

    public ChampSelectBanElement(Dimension dimension) {
        super(dimension);
        this.setBackground(ColorPalette.backgroundColor);
    }

    public void reset() {
        this.completed = false;
        this.championId = 0;
        this.update(-1);
    }

    public void update(ActionObject object) {
        if (completed || object.getChampionId() == 0) return;
        this.championId = object.getChampionId();
        this.completed = object.isCompleted();
        this.update(object.getChampionId());
    }

    public void update(int championId) {
        ResourceLoader.loadResource(String.format(preset, championId), this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (completed || image == null || championId <= 0) return;
        Color infused = new Color((Color.WHITE.getRGB() & 0xFFFFFF) | (0x7F << 24), true);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(infused);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.fillRoundRect(x, y, dimension.width, dimension.height, ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0, ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0);
        g2d.dispose();
    }
}
