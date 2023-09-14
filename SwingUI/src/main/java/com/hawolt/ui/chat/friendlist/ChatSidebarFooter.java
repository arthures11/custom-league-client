package com.hawolt.ui.chat.friendlist;

import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LLabel;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.github.Github;
import com.hawolt.ui.settings.SettingsUI;

import javax.swing.*;
import java.awt.*;

public class ChatSidebarFooter extends ChildUIComponent {
    private static final Font font = new Font("", Font.BOLD, 20);
    private static final int HEIGHT = 30;

    public ChatSidebarFooter(SettingsUI settingsWindow) {
        super(new BorderLayout());
        this.setPreferredSize(new Dimension(0, HEIGHT));
        this.setBackground(ColorPalette.accentColor);

        LFlatButton settingsButton = new LFlatButton("⚙", LTextAlign.CENTER, HighlightType.COMPONENT);
        settingsButton.setBorder(BorderFactory.createEmptyBorder());
        settingsButton.setRounding(ColorPalette.CARD_ROUNDING);
        settingsButton.setRoundingCorners(true, false, false, false);
        settingsButton.setFont(font);
        settingsButton.setPreferredSize(new Dimension(HEIGHT, HEIGHT));
        settingsButton.addActionListener(listener -> {
            if (settingsWindow.isVisible()) {
                settingsWindow.close();
            } else {
                settingsWindow.setVisible(true);
            }
        });
        add(settingsButton, BorderLayout.EAST);
        LLabel version = new LLabel(Github.getCurrentVersion(), LTextAlign.CENTER, true);
        add(version, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //Drawing bot left rounding
        int width = getWidth();
        int height = getHeight();
        g2d.setColor(ColorPalette.accentColor);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
    }
}