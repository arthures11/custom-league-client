package com.hawolt.ui.chat.friendlist;

import com.hawolt.LeagueClientUI;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.queue.GameInvites;
import com.hawolt.ui.queue.QueueState;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 09/08/2023 01:43
 * Author: Twitter @hawolt
 **/

public class ChatSidebarEssentials extends ChildUIComponent {
    private final QueueState state;
    private ChildUIComponent display;
    private ChildUIComponent requests;

    public ChatSidebarEssentials(LeagueClientUI leagueClientUI, IFriendListComponent component) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(0, 5, 5, 5));
        this.setBackground(ColorPalette.accentColor);
        VirtualRiotXMPPClient xmppClient = leagueClientUI.getLeagueClient().getXMPPClient();
        ChatSidebarFriendEssentials essentials = new ChatSidebarFriendEssentials(xmppClient, component);
        this.add(essentials, BorderLayout.CENTER);
        this.state = new QueueState();
        this.state.setVisible(false);
        display = new ChildUIComponent(new BorderLayout());
        display.setBackground(ColorPalette.accentColor);
        display.setBorder(new EmptyBorder(0, 5, 0, 0));
        display.add(state, BorderLayout.NORTH);
        display.add(new GameInvites(leagueClientUI), BorderLayout.CENTER);
        this.add(display, BorderLayout.NORTH);
        requests = new ChildUIComponent(new GridLayout(0, 1, 0, 5));
        requests.setBorder(new EmptyBorder(5, 5, 0, 0));
        requests.setBackground(ColorPalette.accentColor);
        this.add(requests, BorderLayout.SOUTH);
        component.registerNotificationBar(requests);
    }

    public void toggleQueueState(long currentTimeMillis, long estimatedMatchmakingTimeMillis) {
        toggleQueueState(currentTimeMillis, estimatedMatchmakingTimeMillis, false);
    }

    public void toggleQueueState(long currentTimeMillis, long estimatedMatchmakingTimeMillis, boolean lpq) {
        if (state.isLPQ()) state.updateLPQ(estimatedMatchmakingTimeMillis);
        state.setTimer(currentTimeMillis, estimatedMatchmakingTimeMillis, lpq);
        state.setVisible(true);
        revalidate();
    }

    public void disableQueueState() {
        state.setVisible(false);
        revalidate();
    }
}
