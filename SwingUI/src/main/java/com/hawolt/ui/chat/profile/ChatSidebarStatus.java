package com.hawolt.ui.chat.profile;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.ui.generic.component.LComboBox;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.objects.presence.Presence;

import javax.swing.*;
import java.awt.*;

/**
 * Created: 08/08/2023 17:45
 * Author: Twitter @hawolt
 **/

public class ChatSidebarStatus extends JComponent {
    LComboBox<ChatStatus> box;
    private VirtualRiotXMPPClient xmppClient;
    private LeagueClient leagueClient;

    public ChatSidebarStatus() {
        this.setLayout(new BorderLayout());
        box = new LComboBox<>(ChatStatus.values());
        box.setBackground(ColorPalette.accentColor);
        box.setSelectedItem(ChatStatus.DEFAULT);
        box.addItemListener(listener -> {
            if (xmppClient == null) return;
            Presence.Builder builder = leagueClient.getCachedValue(CacheType.PRESENCE_BUILDER);
            String status = box.getItemAt(box.getSelectedIndex()).getStatus();
            xmppClient.setCustomPresence(
                    "default".equals(status) ? "chat" : status,
                    leagueClient.getCachedValue(CacheType.CHAT_STATUS),
                    builder.build()
            );
        });
        this.add(box, BorderLayout.CENTER);
    }

    public String getBoxStatus() {
        return box.getItemAt(box.getSelectedIndex()).getStatus();
    }

    public void setXMPPClient(VirtualRiotXMPPClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    public void setLeagueClient(LeagueClient leagueClient) {
        this.leagueClient = leagueClient;
    }

}
