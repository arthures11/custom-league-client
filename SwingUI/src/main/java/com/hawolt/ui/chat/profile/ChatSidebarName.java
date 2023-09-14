package com.hawolt.ui.chat.profile;

import com.hawolt.ui.generic.themes.ColorPalette;

import javax.swing.*;
import java.awt.*;

/**
 * Created: 08/08/2023 17:45
 * Author: Twitter @hawolt
 **/

public class ChatSidebarName extends JComponent {
    private final Font font = new Font("Dialog", Font.BOLD, 20);
    private String name;

    public ChatSidebarName() {
        this.setBackground(Color.GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (name == null) return;
        Dimension dimension = getSize();
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int y = (dimension.height >> 1) + (metrics.getAscent() >> 1);
        drawHighlightedText(g, name, 0, y);
    }

    public void setSummonerName(String name) {
        this.name = name;
        this.repaint();
    }

    private void drawHighlightedText(Graphics g, String text, int x, int y) {
        g.setColor(Color.BLACK);
        g.drawString(text, x + 1, y + 1);
        g.setColor(ColorPalette.textColor);
        g.drawString(text, x, y);
    }
}
