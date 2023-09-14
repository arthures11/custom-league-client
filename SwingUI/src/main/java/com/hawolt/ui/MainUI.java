package com.hawolt.ui;

import com.hawolt.ui.generic.utility.MainUIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Created: 06/08/2023 13:07
 * Author: Twitter @hawolt
 **/

public class MainUI extends MainUIComponent {
    private static final Integer chatLayer = JLayeredPane.POPUP_LAYER;
    private static final Integer settingsLayer = JLayeredPane.POPUP_LAYER + 1;
    private static final int chatXOffset = 300;
    private static final Dimension chatDimension = new Dimension(400, 300);
    private static final Dimension settingsBorderDimension = new Dimension(100, 50);
    private final JLayeredPane layeredPane;
    private JComponent main;
    private JComponent chat;
    private JComponent settings;

    public MainUI(JFrame frame) {
        super(frame);
        //Setting default theme
        //ColorPalette.setTheme(LThemeChoice.DARK);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(1600, 900));
        this.layeredPane = new JLayeredPane();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (main == null) return;
                adjust();
            }
        });
        this.container.add(this);
        this.add(layeredPane, BorderLayout.CENTER);
        this.init();
    }

    public void setMainComponent(JComponent main) {
        if (this.main != null) this.remove(this.main);
        this.main = main;
        Dimension bounds = getPreferredSize();
        main.setBounds(0, 0, bounds.width, bounds.height);
        this.layeredPane.add(main, JLayeredPane.DEFAULT_LAYER);
    }

    public void adjust() {
        Dimension currentDimension = getSize();
        main.setBounds(0, 0, currentDimension.width, currentDimension.height);
        setChatPosition();
        setSettingsPosition();
        revalidate();
    }

    private void setChatPosition() {
        if (chat == null) return;

        Dimension bounds = getSize();
        chat.setBounds(
                bounds.width - chatXOffset - chatDimension.width,
                bounds.height - chatDimension.height,
                chatDimension.width,
                chatDimension.height
        );
    }

    private void setSettingsPosition() {
        if (settings == null) return;
        Dimension bounds = getSize();
        settings.setBounds(
                settingsBorderDimension.width,
                settingsBorderDimension.height,
                bounds.width - settingsBorderDimension.width * 2,
                bounds.height - settingsBorderDimension.height * 2
        );
    }

    public void addChatComponent(JComponent chat) {
        this.chat = chat;
        this.layeredPane.add(chat, chatLayer);
        this.setChatPosition();
    }

    public void addSettingsComponent(JComponent settings) {
        this.settings = settings;
        this.layeredPane.add(settings, settingsLayer);
        this.setSettingsPosition();
    }
}
