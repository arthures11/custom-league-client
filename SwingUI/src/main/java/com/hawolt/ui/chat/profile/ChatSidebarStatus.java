package com.hawolt.ui.chat.profile;

import com.hawolt.LeagueClientUI;
import com.hawolt.async.presence.PresenceManager;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LComboBox;
import com.hawolt.ui.generic.themes.ColorPalette;

import javax.swing.*;
import java.awt.*;

/**
 * Created: 08/08/2023 17:45
 * Author: Twitter @hawolt
 **/

public class ChatSidebarStatus extends JComponent {
    private final LComboBox<ChatStatus> box;
    private PresenceManager manager;

    public ChatSidebarStatus() {
        this.setLayout(new BorderLayout());
        box = new LComboBox<>(ChatStatus.values());
        box.setBackground(ColorPalette.accentColor);
        box.setSelectedItem(ChatStatus.DEFAULT);
        box.addItemListener(listener -> LeagueClientUI.service.execute(() -> {
            try {
                manager.changeStatus();
            } catch (Exception e) {
                Logger.error(e);
            }
        }));
        this.add(box, BorderLayout.CENTER);
    }

    public void setPresenceManager(PresenceManager manager) {
        this.manager = manager;
    }

    public String getSelectedStatus() {
        return box.getItemAt(box.getSelectedIndex()).getStatus();
    }
}
