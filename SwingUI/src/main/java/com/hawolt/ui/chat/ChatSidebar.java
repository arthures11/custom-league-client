package com.hawolt.ui.chat;

import com.hawolt.LeagueClientUI;
import com.hawolt.ui.chat.friendlist.ChatSidebarEssentials;
import com.hawolt.ui.chat.friendlist.ChatSidebarFooter;
import com.hawolt.ui.chat.friendlist.ChatSidebarFriendlist;
import com.hawolt.ui.generic.component.LScrollPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import javax.swing.*;
import java.awt.*;

/**
 * Created: 08/08/2023 17:08
 * Author: Twitter @hawolt
 **/

public class ChatSidebar extends ChildUIComponent {
    private final ChatSidebarEssentials essentials;
    private final ChatSidebarFooter footer;
    private final ChatSidebarFriendlist list;
    private final ChildUIComponent background;
    private final LScrollPane scrollPane;

    public ChatSidebar(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());
        this.setPreferredSize(new Dimension(300, 0));
        this.setBackground(Color.RED);
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        list = new ChatSidebarFriendlist(leagueClientUI.getChatUI(), leagueClientUI);
        background = new ChildUIComponent(new BorderLayout());
        background.setBackground(ColorPalette.accentColor);
        background.add(list, BorderLayout.NORTH);
        scrollPane = new LScrollPane(background);
        scrollPane.setBackground(ColorPalette.accentColor);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        ChildUIComponent bundle = new ChildUIComponent(new BorderLayout());
        bundle.add(essentials = new ChatSidebarEssentials(leagueClientUI, list), BorderLayout.NORTH);
        bundle.add(scrollPane, BorderLayout.CENTER);
        bundle.add(footer = new ChatSidebarFooter(leagueClientUI.getSettingsUI()), BorderLayout.SOUTH);
        this.add(bundle, BorderLayout.CENTER);
    }

    public ChatSidebarFriendlist getChatSidebarFriendlist() {
        return list;
    }

    public ChatSidebarEssentials getEssentials() {
        return essentials;
    }
}
