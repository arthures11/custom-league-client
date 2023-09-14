package com.hawolt.ui.chat.window;

import com.hawolt.ui.chat.friendlist.IFriendListComponent;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.audio.Sound;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.handler.message.IMessageListener;
import com.hawolt.xmpp.event.objects.conversation.history.impl.FailedMessage;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;
import com.hawolt.xmpp.event.objects.conversation.history.impl.OutgoingMessage;
import com.hawolt.xmpp.event.objects.friends.GenericFriend;
import com.hawolt.xmpp.event.objects.friends.impl.OnlineFriend;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 08/08/2023 20:48
 * Author: Twitter @hawolt
 **/

public class ChatUI extends ChildUIComponent implements IMessageListener, IChatWindow {
    private final Map<String, ChatWindowContent> map = new HashMap<>();
    private final CardLayout layout = new CardLayout();
    private final ChildUIComponent container;
    private final ChatWindowHeader header;
    private VirtualRiotXMPPClient xmppClient;
    private IFriendListComponent component;
    private String lastOpenedChat;
    private ChatWindowContent content;

    public ChatUI() {
        super(new BorderLayout());
        this.add(header = new ChatWindowHeader(new BorderLayout()), BorderLayout.NORTH);
        this.add(container = new ChildUIComponent(this.layout), BorderLayout.CENTER);
    }

    public void setSupplier(VirtualRiotXMPPClient xmppClient) {
        this.xmppClient = xmppClient;
    }

    public ChatWindowHeader getHeader() {
        return header;
    }

    private void addChat(String jid) {
        this.content = new ChatWindowContent(jid, xmppClient, new BorderLayout());
        this.container.add(jid, content);
        this.map.put(jid, content);
        this.revalidate();
    }

    private boolean isChatConfigured(String jid) {
        return this.map.containsKey(jid);
    }

    public ChatWindowContent getChatContainer(String jid) {
        if (!isChatConfigured(jid)) addChat(jid);
        return this.map.get(jid);
    }

    @Override
    public VirtualRiotXMPPClient getXMPPClient() {
        return xmppClient;
    }

    @Override
    public void showChat(GenericFriend friend) {
        this.lastOpenedChat = friend.getJID();
        if (component != null) component.onOpen(friend.getJID());
        if (!isVisible()) setVisible(true);
        this.getHeader().setTarget(
                friend instanceof OnlineFriend ?
                        ((OnlineFriend) friend).getLOLName() :
                        String.join(
                                "#",
                                friend.getName().toString(),
                                friend.getTagline().toString()
                        )
        );
        if (!isChatConfigured(lastOpenedChat)) addChat(lastOpenedChat);
        this.layout.show(container, lastOpenedChat);
        getChatContainer(friend.getJID()).drain();
        this.content.getInput().grabFocus();
    }

    @Override
    public void setIFriendListComponent(IFriendListComponent component) {
        this.component = component;
    }

    @Override
    public boolean isChatOpened(String jid) {
        return isVisible() && jid.equals(lastOpenedChat);
    }

    @Override
    public void onMessageReceived(IncomingMessage incomingMessage) {
        if (component != null) component.onMessage(incomingMessage.getFrom());
        if (incomingMessage.getType().equals("groupchat")) return;
        ChatWindowContent content = getChatContainer(incomingMessage.getFrom());
        content.addMessage(ChatPerspective.OTHER, incomingMessage.getBody());
        boolean isChatOpen = isChatOpened(incomingMessage.getFrom());
        AudioEngine.play(isChatOpen ? Sound.ACTIVE_CHAT_MESSAGE : Sound.INACTIVE_CHAT_MESSAGE);
        if (xmppClient != null) {
            xmppClient.markChatAsRead(incomingMessage.getFrom());
        }
    }

    @Override
    public void onMessageSent(OutgoingMessage outgoingMessage) {

    }

    @Override
    public void onFailedMessage(FailedMessage failedMessage) {

    }
}
