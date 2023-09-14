package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.async.ExecutorManager;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.data.ChampSelectPhase;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.generic.themes.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created: 10/09/2023 16:38
 * Author: Twitter @hawolt
 **/

public abstract class ChampSelectHeaderUI extends ChampSelectUIComponent {
    protected final ScheduledExecutorService scheduler = ExecutorManager.getScheduledService(getClass().getSimpleName());
    protected double currentTotalTimeMillis, currentTimeRemainingMillis, timestamp;
    protected ChampSelectPhase phase;

    public ChampSelectHeaderUI() {
        ColorPalette.addThemeListener(this);
        this.setBackground(ColorPalette.backgroundColor);
        this.setPreferredSize(new Dimension(0, 60));
        this.scheduler.scheduleAtFixedRate(this::repaint, 0, 20, TimeUnit.MILLISECONDS);
        this.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 1, Color.DARK_GRAY),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
    }

    public void reset() {
        this.timestamp = 0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dimension = getSize();

        if (context != null && timestamp != 0L) {
            double difference = currentTotalTimeMillis - currentTimeRemainingMillis + (System.currentTimeMillis() - timestamp);
            double percentage = difference / currentTotalTimeMillis;
            if (percentage < 0) percentage = 0;
            int centerX = (dimension.width >> 1);
            int extend = (int) Math.ceil(centerX * percentage);
            g.setColor(Color.WHITE);
            g.fillRect(centerX, dimension.height - 5, extend, 5);
            g.fillRect(centerX - extend, dimension.height - 5, extend, 5);
        }

        if (phase == null) return;
        String status = getPhaseDescription(phase);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setColor(Color.WHITE);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(new Font("Dialog", Font.BOLD, 28));
        FontMetrics metrics = graphics2D.getFontMetrics();
        int width = metrics.stringWidth(status);
        graphics2D.drawString(status, (dimension.width >> 1) - (width >> 1), (dimension.height >> 1) + (metrics.getAscent() >> 1));
    }

    @Override
    public void update() {
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        this.currentTimeRemainingMillis = settingsContext.getCurrentTimeRemainingMillis();
        this.currentTotalTimeMillis = settingsContext.getCurrentTotalTimeMillis();
        this.timestamp = System.currentTimeMillis();
        this.phase = getChampSelectPhase();
    }

    protected abstract String getPhaseDescription(ChampSelectPhase phase);

    protected abstract ChampSelectPhase getChampSelectPhase();
}
