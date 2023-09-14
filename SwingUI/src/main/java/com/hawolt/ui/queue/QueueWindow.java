package com.hawolt.ui.queue;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.PartiesRegistration;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyRole;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyType;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.Base64GZIP;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.chat.friendlist.ChatSidebarEssentials;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LLabel;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.queue.pop.QueueDialog;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.audio.Sound;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Created: 10/08/2023 15:52
 * Author: Twitter @hawolt
 **/

public class QueueWindow extends ChildUIComponent implements Runnable, PacketCallback, IServiceMessageListener<RiotMessageServiceMessage> {
    private final CardLayout layout = new CardLayout();
    private final LeagueClientUI leagueClientUI;
    private final ChildUIComponent parent;
    private final DraftQueueLobby lobby;
    private final TFTQueueLobby tftLobby;
    private final List<String> supportedModes = Arrays.asList("ARAM", "BOTS", "BLIND", "DRAFT", "RANKED-FLEX", "RANKED-SOLO", "TFT");
    Boolean init = false;
    ChildUIComponent main = new ChildUIComponent(new BorderLayout());
    private LFlatButton button = new LFlatButton("Show Lobby", LTextAlign.CENTER);


    public QueueWindow(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());
        this.leagueClientUI = leagueClientUI;
        this.add(parent = new ChildUIComponent(layout), BorderLayout.CENTER);
        lobby = new DraftQueueLobby(leagueClientUI, parent, layout, this);
        tftLobby = new TFTQueueLobby(leagueClientUI, parent, layout, this);
        LeagueClientUI.service.execute(this);
    }

    public DraftQueueLobby getDraftLobby() {
        Component[] components = this.parent.getComponents();
        boolean alreadyShown = false;
        for (int i = 0; i < components.length; i++) {
            if (!alreadyShown && components[i].getClass().getCanonicalName().equals("com.hawolt.ui.queue.DraftQueueLobby"))
                alreadyShown = true;
            if (components[i].getClass().getCanonicalName().equals("com.hawolt.ui.queue.TFTQueueLobby"))
                this.parent.remove(i);
        }
        if (!alreadyShown) {
            this.parent.add("lobby", lobby);
        }
        layout.show(parent, "lobby");
        return lobby;
    }

    public TFTQueueLobby getTftLobby() {
        Component[] components = this.parent.getComponents();
        boolean alreadyShown = false;
        for (int i = 0; i < components.length; i++) {
            if (!alreadyShown && components[i].getClass().getCanonicalName().equals("com.hawolt.ui.queue.TFTQueueLobby"))
                alreadyShown = true;
            if (components[i].getClass().getCanonicalName().equals("com.hawolt.ui.queue.DraftQueueLobby"))
                this.parent.remove(i);
        }
        if (!alreadyShown) {
            this.parent.add("lobby", tftLobby);
        }
        layout.show(parent, "lobby");
        return tftLobby;
    }

    @Override
    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) throws Exception {
        TypedObject data = typedObject.getTypedObject("data");
        TypedObject message = data.getTypedObject("flex.messaging.messages.AcknowledgeMessage");
        String body = Base64GZIP.unzipBase64(message.getString("body"));
        JSONArray array = new JSONArray(body);
        Map<String, List<JSONObject>> map = new HashMap<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            String state = object.getString("queueState");
            if ("OFF".equals(state)) continue;
            String shortName = object.getString("shortName");
            boolean supported = false;
            for (String mode : supportedModes) {
                if (shortName.contains(mode)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) continue;
            String gameMode = object.getString("gameMode");
            if (!map.containsKey(gameMode)) map.put(gameMode, new ArrayList<>());
            map.get(gameMode).add(object);
        }
        ChildUIComponent modes = new ChildUIComponent(new GridLayout(0, (int) map.keySet().stream().filter(o -> !o.contains("TUTORIAL")).count(), 5, 0));
        modes.setBorder(new EmptyBorder(5, 5, 5, 5));
        main.setBackground(ColorPalette.backgroundColor);
        modes.setBackground(ColorPalette.backgroundColor);
        for (String key : map.keySet()) {
            if (key.contains("TUTORIAL")) continue;
            ChildUIComponent parent = new ChildUIComponent(new BorderLayout());
            ChildUIComponent grid = new ChildUIComponent(new GridLayout(0, 1, 0, 5)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    //background issues fix if buttons are all grid long
                    g2d.setColor(ColorPalette.backgroundColor);
                    g2d.fillRect(getX(), getY(), getWidth(), getHeight());
                    g2d.setColor(ColorPalette.cardColor);
                    g2d.fillRoundRect(getX(), getY(), getWidth(), getHeight(), ColorPalette.CARD_ROUNDING, ColorPalette.CARD_ROUNDING);
                    g2d.dispose();
                }
            };
            parent.setBackground(ColorPalette.backgroundColor);

            //Mode label
            LLabel label = new LLabel(key, LTextAlign.CENTER, true);

            grid.add(label);

            for (JSONObject object : map.get(key)) {
                String name = object.getString("shortName");
                if (name.contains("TUTORIAL") || name.contains("CLASH")) {
                    continue;
                }

                //parse mode name
                String modeName = "";
                if (name.contains("NORMAL")) {
                    modeName = "Normal";
                    if (name.contains("DRAFT"))
                        modeName += " Draft";
                    else if (name.contains("BLIND"))
                        modeName += " Blind";
                } else if (name.contains("RANKED")) {
                    modeName = "Ranked";
                    if (name.contains("SOLO"))
                        modeName += " Solo/Duo";
                    else if (name.contains("FLEX"))
                        modeName += " Flex";
                    else if (name.contains("TURBO"))
                        modeName = "Hyper Roll";
                    else if (name.contains("DOUBLE"))
                        modeName = "Double Up";
                } else if (name.contains("BOTS")) {
                    modeName = "Bots";
                    if (name.contains("INTRO"))
                        modeName += " Intro";
                    else if (name.contains("EASY"))
                        modeName += " Easy";
                    else if (name.contains("MEDIUM"))
                        modeName += " Medium";
                } else if (name.contains("ARAM")) {
                    modeName = "ARAM";
                }

                LFlatButton button = new LFlatButton(modeName.isEmpty() ? name : modeName, LTextAlign.CENTER, HighlightType.COMPONENT);

                button.setPreferredSize(new Dimension(grid.getWidth() / 4, 30));

                button.setActionCommand(object.toString());
                if (key.contains("CLASSIC")) {
                    button.addActionListener(e -> goToLobby(e, "CLASSIC"));
                } else if (key.contains("TFT")) {
                    button.addActionListener(e -> goToLobby(e, "TFT"));
                } else if (key.contains("ARAM")) {
                    button.addActionListener(e -> goToLobby(e, "ARAM"));
                }
                grid.add(button);
            }
            parent.add(grid, BorderLayout.NORTH);
            parent.setPreferredSize(new Dimension(modes.getWidth() / 4, 0));
            modes.add(parent);
        }
        main.add(modes, BorderLayout.CENTER);

        this.parent.add("modes", main);
        layout.show(parent, "modes");
        revalidate();
    }

    @Override
    public void run() {
        try {
            LeagueClient client = leagueClientUI.getLeagueClient();
            client.getRMSClient().getHandler().addMessageServiceListener(MessageService.TEAMBUILDER, this);
            client.getRTMPClient().getMatchMakerService().getAllQueuesCompressedAsynchronous(this);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        JSONObject payload = riotMessageServiceMessage.getPayload().getPayload();
        if (payload.has("backwardsTransitionInfo")) {
            JSONObject info = payload.getJSONObject("backwardsTransitionInfo");
            if (!info.has("backwardsTransitionReason")) return;
            handleBackwardsTransitionReason(info);
            leagueClientUI.getChatSidebar().getEssentials().disableQueueState();
            try {
                leagueClientUI.getLeagueClient().getLedge().getParties().ready();
            } catch (IOException e) {
                Logger.error(e);
            }
        } else if (payload.has("phaseName")) {
            String phaseName = payload.getString("phaseName");
            ChatSidebarEssentials essentials = leagueClientUI.getChatSidebar().getEssentials();
            if (phaseName.equals("MATCHMAKING")) {
                JSONObject matchmakingState = payload.getJSONObject("matchmakingState");
                if (matchmakingState.has("backwardsTransitionReason")) {
                    handleBackwardsTransitionReason(matchmakingState);
                }
                long estimatedMatchmakingTimeMillis = matchmakingState.getLong("estimatedMatchmakingTimeMillis");
                if (payload.getInt("counter") != 0) return;
                essentials.toggleQueueState(System.currentTimeMillis(), estimatedMatchmakingTimeMillis);
                revalidate();
            } else if (phaseName.equals("AFK_CHECK")) {
                AudioEngine.play(Sound.QUEUE_POP);
                JSONObject afkCheckState = payload.getJSONObject("afkCheckState");
                long maxAfkMillis = afkCheckState.getLong("maxAfkMillis");
                QueueDialog dialog = new QueueDialog(Frame.getFrames()[0], "Queue Notification", maxAfkMillis);
                if (dialog.showQueueDialog().getSelection() != 1) {
                    essentials.disableQueueState();
                } else {
                    try {
                        MatchContext context = leagueClientUI.getLeagueClient().getLedge().getTeamBuilder().indicateAfkReadiness();
                        Logger.info("Queue Accept: {}", context.getStatus());
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                }
            } else {
                Logger.info("Ignored RMS Packet {}", riotMessageServiceMessage);
            }
        }
    }

    private void handleBackwardsTransitionReason(JSONObject info) {
        /*
         * other existing backwardsTransitionReason
         * 1.   AFK_CHECK_FAILED
         */
        switch (info.getString("backwardsTransitionReason")) {
            case "PLAYER_TIMED_OUT_ON_REQUIRED_ACTION",
                    "PLAYER_LEFT_CHAMPION_SELECT",
                    "PLAYER_LEFT_MATCHMAKING" -> {
                leagueClientUI.getLayoutManager().getChampSelectUI().showBlankPanel();
            }
        }
    }

    public void goToLobby(ActionEvent e, String mode) {
        Logger.error("goTo Lobby mode: " + mode);
        if (mode.equals("CLASSIC") || mode.equals("ARAM")) {
            this.parent.add("lobby", lobby);
            layout.show(parent, "lobby");
            lobby.actionPerformed(null);
        } else if (mode.equals("TFT")) {
            this.parent.add("lobby", tftLobby);
            layout.show(parent, "lobby");
            lobby.actionPerformed(null);
        }
        if (!init) {
            LFlatButton button = new LFlatButton("Show Lobby", LTextAlign.CENTER, HighlightType.COMPONENT);
            button.setPreferredSize(new Dimension(getWidth() / 5, 30));
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setVerticalAlignment(SwingConstants.CENTER);
            button.addActionListener(listener -> layout.show(parent, "lobby"));
            main.add(button, BorderLayout.SOUTH);
            init = true;
        }
        JSONObject json = new JSONObject(e.getActionCommand());
        long queueId = json.getLong("id");
        long maximumParticipantListSize = json.getLong("maximumParticipantListSize");
        PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
        try {
            PartiesRegistration registration = partiesLedge.getCurrentRegistration();
            if (registration == null) partiesLedge.register();
            partiesLedge.role(PartyRole.DECLINED);
            partiesLedge.gamemode(
                    partiesLedge.getCurrentPartyId(),
                    maximumParticipantListSize,
                    0,
                    queueId
            );
            partiesLedge.partytype(PartyType.OPEN);
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }

    public void rebase() {
        main.remove(button);
        init = false;
    }
}
