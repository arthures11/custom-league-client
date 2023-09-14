package com.hawolt.ui.chat.friendlist;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.misc.SortOrder;
import com.hawolt.ui.champselect.context.ChampSelectDataContext;
import com.hawolt.ui.champselect.context.impl.ChampSelect;
import com.hawolt.ui.chat.window.IChatWindow;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LLabel;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.audio.Sound;
import com.hawolt.util.settings.SettingListener;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.EventListener;
import com.hawolt.xmpp.event.handler.presence.IPresenceListener;
import com.hawolt.xmpp.event.objects.friends.FriendList;
import com.hawolt.xmpp.event.objects.friends.GenericFriend;
import com.hawolt.xmpp.event.objects.friends.IFriendListener;
import com.hawolt.xmpp.event.objects.friends.impl.OnlineFriend;
import com.hawolt.xmpp.event.objects.friends.status.FailedFriendStatus;
import com.hawolt.xmpp.event.objects.presence.GenericPresence;
import com.hawolt.xmpp.event.objects.presence.impl.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Created: 08/08/2023 18:11
 * Author: Twitter @hawolt
 **/

public class ChatSidebarFriendlist extends ChildUIComponent implements SettingListener<String>, IFriendListener, IPresenceListener, IFriendListComponent, EventListener<FriendList> {
    private final ChatListComparator alphabeticalComparator = new ChatListComparator(ChatListComparatorType.NAME, SortOrder.ASCENDING);
    private final ChatListComparator statusComparator = new ChatListComparator(ChatListComparatorType.STATUS, SortOrder.DESCENDING);
    private final Map<String, ChatSidebarFriend> map = new HashMap<>();
    private final IChatWindow window;
    private final Object lock = new Object();
    private final List<GenericPresence> buffer = new LinkedList<>();
    private String name, friendHandling = "User choice";
    private JComponent component;
    private LeagueClientUI leagueClientUI;
    private Map<GenericFriend, ChildUIComponent> tmp = new HashMap<>();

    public ChatSidebarFriendlist(IChatWindow window, LeagueClientUI leagueClientUI) {
        super(new GridLayout(0, 1, 0, 0));
        this.window = window;
        this.window.setIFriendListComponent(this);
        this.leagueClientUI = leagueClientUI;
        this.leagueClientUI.getSettingService().addSettingListener("autoFriends", this);
        setBackground(ColorPalette.accentColor);
    }

    @Override
    public void search(String name) {
        this.name = name;
        this.filter(name);
    }

    private void filter(String name) {
        List<ChatSidebarFriend> list = new ArrayList<>(map.values());
        for (ChatSidebarFriend sidebar : list) {
            GenericFriend friend = sidebar.getFriend();
            String computed = friend instanceof OnlineFriend ?
                    ((OnlineFriend) friend).getLOLName() :
                    String.join(
                            "#",
                            friend.getName().toString(),
                            friend.getTagline().toString()
                    );

            synchronized (lock) {
                if (name.isEmpty() || computed.toLowerCase().contains(name)) {
                    sidebar.setEnabled(true);
                    add(sidebar);
                } else {
                    sidebar.setEnabled(false);
                    remove(sidebar);
                }
            }
        }
        sort();
        revalidate();
    }

    @Override
    public void registerNotificationBar(JComponent component) {
        this.component = component;
    }

    @Override
    public void onMessage(String jid) {
        if (window.isChatOpened(jid)) return;
        if (!map.containsKey(jid)) return;
        this.getComponent(jid).increment();
    }

    @Override
    public void onOpen(String jid) {
        this.getComponent(jid).opened();
    }

    public void addFriendComponent(ChatSidebarFriend component) {
        this.map.put(component.getFriend().getJID(), component);
        synchronized (lock) {
            this.add(component);
        }
    }

    public void removeFriendComponent(String jid) {
        if (!map.containsKey(jid)) return;
        ChatSidebarFriend friend = map.get(jid);
        map.remove(jid);
        synchronized (lock) {
            remove(friend);
            revalidate();
        }
    }

    private ChatSidebarFriend getComponent(String jid) {
        return map.get(jid);
    }

    private void sort() {
        synchronized (lock) {
            removeAll();
            map.values()
                    .stream()
                    .filter(ChatSidebarFriend::isEnabled)
                    .sorted(alphabeticalComparator)
                    .sorted(statusComparator)
                    .forEach(this::add);
        }
        repaint();
        revalidate();
    }

    private void handle(GenericPresence presence) {
        if (!map.containsKey(presence.getBareFromJID())) {
            buffer.add(presence);
        } else {
            getComponent(presence.getBareFromJID()).setLastKnownPresence(presence);
            if (name != null && !name.isEmpty()) filter(name);
            sort();
        }
    }

    private void addFriendComponent(GenericFriend friend) {
        ChatSidebarFriend button = new ChatSidebarFriend(window.getXMPPClient(), friend, leagueClientUI);
        button.setRoundingCorners(false, true, false, true);
        button.executeOnClick(() -> window.showChat(friend));
        addFriendComponent(button);
    }

    //TODO ah no no no
    private void addPendingFriendRequest(GenericFriend genericFriend, boolean incoming) {
        ChildUIComponent request = new ChildUIComponent(new BorderLayout(5, 0));
        request.setBackground(ColorPalette.accentColor);
        LLabel name = new LLabel(genericFriend.getName().toString(), LTextAlign.LEFT, true);
        request.add(name, BorderLayout.CENTER);
        request.setPreferredSize(new Dimension(0, 30));
        ChildUIComponent actions = new ChildUIComponent(new GridLayout(0, incoming ? 2 : 1, 5, 0));
        actions.setBackground(ColorPalette.accentColor);
        switch (friendHandling) {
            case "User choice" -> {
                if (incoming) {
                    LFlatButton accept = new LFlatButton("+", LTextAlign.CENTER, HighlightType.COMPONENT);
                    accept.addActionListener(listener -> {
                        VirtualRiotXMPPClient client = window.getXMPPClient();
                        List<GenericFriend> list = client.getFriendList().find(friend -> genericFriend.getName().equals(friend.getName()));
                        if (list.isEmpty()) return;
                        client.addFriendByTag(list.get(0).getName().toString(), list.get(0).getTagline().toString());
                        synchronized (lock) {
                            component.remove(request);
                        }
                        component.revalidate();
                    });
                    accept.setPreferredSize(new Dimension(30, 0));
                    actions.add(accept);
                }
                LFlatButton remove = new LFlatButton("×", LTextAlign.CENTER, HighlightType.COMPONENT);
                remove.addActionListener(listener -> {
                    VirtualRiotXMPPClient client = window.getXMPPClient();
                    List<GenericFriend> list = client.getFriendList().find(friend -> genericFriend.getName().equals(friend.getName()));
                    if (list.isEmpty()) return;
                    client.removeFriend(list.get(0).getJID());
                    synchronized (lock) {
                        component.remove(request);
                    }
                });
                remove.setPreferredSize(new Dimension(30, 0));
                actions.add(remove);
                tmp.put(genericFriend, request);
                request.add(actions, BorderLayout.EAST);
                synchronized (lock) {
                    this.component.add(request);
                }
            }
            case "Auto accept" -> {
                VirtualRiotXMPPClient client = window.getXMPPClient();
                List<GenericFriend> list = client.getFriendList().find(friend -> genericFriend.getName().equals(friend.getName()));
                if (list.isEmpty()) return;
                client.addFriendByTag(list.get(0).getName().toString(), list.get(0).getTagline().toString());
            }
            case "Auto reject" -> {
                VirtualRiotXMPPClient client = window.getXMPPClient();
                List<GenericFriend> list = client.getFriendList().find(friend -> genericFriend.getName().equals(friend.getName()));
                if (list.isEmpty()) return;
                client.removeFriend(list.get(0).getJID());
            }
        }
    }


    @Override
    public void onFriendRemove(GenericFriend genericFriend) {
        removeFriendComponent(genericFriend.getJID());
    }

    @Override
    public void onIncomingFriendRequest(GenericFriend genericFriend) {
        if (this.component == null) return;
        AudioEngine.play(Sound.FRIEND_REQUEST);
        addPendingFriendRequest(genericFriend, true);
        component.revalidate();
    }

    @Override
    public void onOutgoingFriendRequest(GenericFriend genericFriend) {
        if (this.component == null) return;
        addPendingFriendRequest(genericFriend, false);
        component.revalidate();
    }

    @Override
    public void onIncomingFriendRequestRevoked(GenericFriend genericFriend) {

    }

    @Override
    public void onIncomingFriendRequestAccepted(GenericFriend genericFriend) {
        addFriendComponent(genericFriend);
        revalidate();
    }

    @Override
    public void onOutgoingFriendRequestCanceled(GenericFriend genericFriend) {
        if (tmp.containsKey(genericFriend)) {
            synchronized (lock) {
                component.remove(tmp.get(genericFriend));
            }
            component.revalidate();
        }
    }

    @Override
    public void onOutgoingFriendRequestAccepted(GenericFriend genericFriend) {
        if (tmp.containsKey(genericFriend)) {
            synchronized (lock) {
                component.remove(tmp.get(genericFriend));
            }
            component.revalidate();
        }
        addFriendComponent(genericFriend);
        revalidate();
    }

    @Override
    public void onFailedInteraction(FailedFriendStatus failedFriendStatus) {

    }

    @Override
    public void onEvent(FriendList friendList) {
        for (GenericFriend friend : friendList.get()) {
            switch (friend.getType()) {
                case BOTH -> addFriendComponent(friend);
                case PENDING_IN -> addPendingFriendRequest(friend, true);
                case PENDING_OUT -> addPendingFriendRequest(friend, false);
            }
        }
        if (component != null) component.revalidate();
        for (int i = buffer.size() - 1; i >= 0; i--) {
            handle(buffer.remove(i));
        }
        revalidate();
    }

    @Override
    public void onUnfriendPresence(UnfriendPresence presence) {
        handle(presence);
    }

    @Override
    public void onUnknownPresence(GenericPresence presence) {
        handle(presence);
    }

    @Override
    public void onOfflinePresence(OfflinePresence presence) {
        handle(presence);
    }

    @Override
    public void onMobilePresence(MobilePresence presence) {
        handle(presence);
    }

    @Override
    public void onBasicPresence(BasicPresence presence) {
        handle(presence);
    }

    @Override
    public void onMucPresence(MucPresence presence) {
        handle(presence);
    }

    @Override
    public void onJoinMucPresence(JoinMucPresence presence) {
        ChampSelect champSelect = leagueClientUI.getLayoutManager().getChampSelectUI().getChampSelect();
        ChampSelectDataContext dataContext = champSelect.getChampSelectDataContext();
        dataContext.push(presence);
        handle(presence);
    }

    @Override
    public void onLeaveMucPresence(LeaveMucPresence presence) {
        handle(presence);
    }

    @Override
    public void onSettingWrite(String name, String value) {
        this.friendHandling = value;
    }
}
