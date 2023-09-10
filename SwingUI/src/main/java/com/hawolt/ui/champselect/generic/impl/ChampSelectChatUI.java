package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.data.ChampSelectTeamMember;
import com.hawolt.ui.champselect.data.ChampSelectTeamType;
import com.hawolt.ui.champselect.data.TeamMemberFunction;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.custom.LHintTextField;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.ui.LScrollPane;
import com.hawolt.util.ui.SmartScroller;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;
import com.hawolt.xmpp.event.objects.presence.impl.JoinMucPresence;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created: 29/08/2023 18:33
 * Author: Twitter @hawolt
 **/

public class ChampSelectChatUI extends ChampSelectUIComponent {
    private final JTextArea document;
    private final JTextField input;

    private MatchContext matchContext;

    public ChampSelectChatUI() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        LScrollPane scrollPane = new LScrollPane(document = new JTextArea());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        SmartScroller.configure(scrollPane);
        this.document.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.document.setFont(new Font("Dialog", Font.PLAIN, 19));
        this.document.setEditable(false);
        this.document.setLineWrap(true);
        this.document.setForeground(Color.WHITE);
        this.add(scrollPane, BorderLayout.CENTER);
        this.document.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(input = new LHintTextField("Send a message..."), BorderLayout.SOUTH);
        this.setPreferredSize(new Dimension(0, 200));
        this.input.addActionListener(listener -> {
            if (matchContext == null) {
                document.append("> You are currently not in a chatroom." + System.lineSeparator());
                this.input.setText("");
            } else {
                String domain = String.format("champ-select.%s.pvp.net", matchContext.getPayload().getTargetRegion());
                String jid = String.format("%s@%s", matchContext.getPayload().getChatRoomName(), domain);
                LeagueClient client = context.getChampSelectDataContext().getLeagueClient();
                client.getXMPPClient().sendGroupMessage(jid, input.getText(), null);
                this.input.setText("");
            }
        });
    }

    public void push(IncomingMessage incomingMessage) {
        String source = incomingMessage.getFrom().split("@")[0];
        if (matchContext == null || !matchContext.getPayload().getChatRoomName().equals(source)) return;
        String puuid = incomingMessage.getRC();
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        Arrays.stream(settingsContext.getCells(ChampSelectTeamType.ALLIED, TeamMemberFunction.INSTANCE))
                .map(o -> (ChampSelectTeamMember) o)
                .filter(o -> puuid.equalsIgnoreCase(o.getPUUID()))
                .findAny()
                .ifPresent(member -> handle(member, incomingMessage));
    }

    public void push(JoinMucPresence presence) {
        String puuid = presence.getParticipant().getJid().split("@")[0];
        if (context == null) cache.add(0, puuid + "has joined the chat");
        else {
            Map<String, String> resolver = context.getChampSelectDataContext().getPUUIDResolver();
            if (!cache.isEmpty()) forward(resolver);
            if (!resolver.containsKey(puuid) || !cache.isEmpty()) {
                cache.add(0, puuid + "has joined the chat");
            } else {
                handle(resolver.get(puuid), "has joined the chat");
            }
        }
    }

    private final List<String> cache = new ArrayList<>();

    private void handle(ChampSelectTeamMember member, IncomingMessage incomingMessage) {
        Map<String, String> resolver = context.getChampSelectDataContext().getPUUIDResolver();
        if (!cache.isEmpty()) forward(resolver);
        if (!resolver.containsKey(member.getPUUID()) || !cache.isEmpty()) {
            cache.add(0, member.getPUUID() + incomingMessage.getBody());
        } else {
            handle(resolver.get(member.getPUUID()), incomingMessage.getBody());
        }
    }

    private void handle(String name, String body) {
        String message = String.format("%s: %s", name, body);
        handle(message);
    }

    private void handle(String message) {
        document.append(String.format("%s%s", message, System.lineSeparator()));
        revalidate();
        repaint();
    }

    private void forward(Map<String, String> resolver) {
        for (int i = cache.size() - 1; i >= 0; i--) {
            String data = cache.get(i);
            String puuid = data.substring(0, 36);
            String message = data.substring(36);
            if (!resolver.containsKey(puuid)) break;
            handle(resolver.get(puuid), message);
            cache.remove(i);
        }
    }

    public void setMatchContext(MatchContext context) {
        this.document.setText("");
        this.matchContext = context;
    }
}