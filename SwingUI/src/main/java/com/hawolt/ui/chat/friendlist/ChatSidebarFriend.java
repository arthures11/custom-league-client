package com.hawolt.ui.chat.friendlist;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.PartyException;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyRole;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.generic.data.Unsafe;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.util.paint.PaintHelper;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.objects.friends.GenericFriend;
import com.hawolt.xmpp.event.objects.friends.impl.OnlineFriend;
import com.hawolt.xmpp.event.objects.presence.ConnectionStatus;
import com.hawolt.xmpp.event.objects.presence.GenericPresence;
import com.hawolt.xmpp.event.objects.presence.games.BasicGame;
import com.hawolt.xmpp.event.objects.presence.games.GameType;
import com.hawolt.xmpp.event.objects.presence.games.impl.*;
import com.hawolt.xmpp.event.objects.presence.impl.BasicPresence;
import com.hawolt.xmpp.event.objects.presence.impl.MobilePresence;
import com.hawolt.xmpp.event.objects.presence.impl.OfflinePresence;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.Map;

/**
 * Created: 08/08/2023 18:15
 * Author: Twitter @hawolt
 **/

public class ChatSidebarFriend extends LFlatButton {
    private final VirtualRiotXMPPClient xmppClient;
    private final LeagueClientUI leagueClientUI;
    private final GenericFriend friend;
    private final Font statusFont = new Font("Dialog", Font.BOLD, 12);
    private GenericPresence lastKnownPresence;
    private String providedUsername;
    private Runnable runnable;
    private int counter;
    private ConnectionStatus connectionStatus;
    private String status;
    private Color color;

    public ChatSidebarFriend(VirtualRiotXMPPClient xmppClient, GenericFriend friend, LeagueClientUI leagueClientUI) {
        super();
        setHighlightType(HighlightType.COMPONENT);
        this.setPreferredSize(new Dimension(0, 50));
        this.friend = friend;
        this.xmppClient = xmppClient;
        this.leagueClientUI = leagueClientUI;
        this.providedUsername = getProvidedUsername();
        this.setFont(new Font("Dialog", Font.BOLD, 18));
        this.setText(providedUsername);
        this.color = getBaseColor();
        setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                clickEvent(evt);
            }
        });
    }

    public String getProvidedUsername() {
        return friend instanceof OnlineFriend ?
                ((OnlineFriend) friend).getLOLName() :
                String.join(
                        "#",
                        friend.getName().toString(),
                        friend.getTagline().toString()
                );
    }

    public GenericFriend getFriend() {
        return friend;
    }

    private Color getBaseColor() {
        return switch (getConnectionStatus()) {
            case ONLINE -> ColorPalette.friendOnline;
            case MOBILE -> ColorPalette.friendMobile;
            default -> ColorPalette.friendOffline;
        };
    }

    private void handleLOR(LOR gameInfo) {
        this.color = ColorPalette.friendInOtherGame;
        if ("bacon_availability_in_game".equals(gameInfo.getAvailability())) {
            this.status = "Playing Legends of Runeterra";
        } else {
            this.status = "Legends of Runeterra";
        }
    }

    private void handleWildrift(Wildrift gameInfo) {
        this.color = ColorPalette.friendInOtherGame;
        this.status = "Playing Wildrift";
    }

    private void handleValorant(Valorant gameInfo) {
        this.color = ColorPalette.friendInOtherGame;
        String sessionLoopState = gameInfo.getSessionLoopState();
        if (sessionLoopState != null) {
            if (sessionLoopState.contains("INGAME")) {
                this.status = "Playing Valorant";
            } else {
                this.status = "Valorant";
            }
        } else {
            this.status = "Unknown Valorant";
        }
    }

    private void handleRiotMobile(Ritoplus gameInfo) {
        this.color = ColorPalette.friendMobile;
        this.status = "Riot Mobile";
    }

    private void handleLOL(BasicPresence presence, LOL gameInfo) {
        if ("away".equals(presence.getShow())) {
            this.color = ColorPalette.friendDND;
            this.status = "Do not Disturb";
        } else {
            switch (gameInfo.getLeagueGameStatus()) {
                case CHAMP_SELECT, QUEUE, IN_GAME -> this.color = ColorPalette.friendInGame;
            }
            String gameStatus = switch (gameInfo.getLeagueGameStatus()) {
                case ONLINE -> {
                    if (gameInfo.getMessage().isEmpty()) {
                        yield "Online";
                    } else {
                        yield "Online \"" + gameInfo.getMessage() + "\"";
                    }
                }
                case IN_LOBBY -> "Lobby";
                case OPEN_LOBBY -> "Lobby (Open)";
                case CLOSED_LOBBY -> "Lobby (Closed)";
                case FULL_LOBBY -> "Lobby (Full)";
                case CHAMP_SELECT -> "Champselect";
                case QUEUE -> "In Queue";
                case IN_GAME -> "Playing";
                case UNKNOWN -> "Idle";
            };
            String type = translate(gameInfo.getLeagueQueueType().name());
            String gameType = switch (gameInfo.getLeagueQueueType()) {
                case TFT, TFT_RANKED, DOUBLE_UP, HYPER_ROLL -> String.format("TFT %s", type);
                case PRACTICE_TOOL, NORMAL, FLEX, SOLO_DUO, ARAM -> type;
                case UNKNOWN -> null;
            };
            if (gameType != null) {
                this.status = String.join(" ", gameStatus, gameType);
            } else {
                this.status = gameStatus;
            }
        }
    }

    private String translate(String name) {
        StringBuilder base = new StringBuilder(name.replace("TFT_", "").toLowerCase());
        base.setCharAt(0, Character.toUpperCase(base.charAt(0)));
        for (int i = base.length() - 1; i >= 1; i--) {
            if (base.charAt(i) == '_') {
                if (i + 1 <= base.length() - 1) base.setCharAt(i + 1, Character.toUpperCase(base.charAt(i + 1)));
                base.setCharAt(i, ' ');
            }
        }
        return base.toString();
    }

    public GenericPresence getLastKnownPresence() {
        return lastKnownPresence;
    }

    public void setLastKnownPresence(GenericPresence lastKnownPresence) {
        this.lastKnownPresence = lastKnownPresence;
        this.connectionStatus = getConnectionStatus();
        this.color = getBaseColor();
        Map<String, BasicGame> map = lastKnownPresence.getGameMap();
        for (String key : map.keySet()) {
            if ("keystone".equals(key)) continue;
            switch (key) {
                case "league_of_legends" -> handleLOL(
                        Unsafe.cast(lastKnownPresence),
                        lastKnownPresence.getGameInfo(GameType.LEAGUE_OF_LEGENDS,
                                null)
                );
                case "valorant" -> handleValorant(lastKnownPresence.getGameInfo(GameType.VALORANT, null));
                case "ritoplus" -> handleRiotMobile(lastKnownPresence.getGameInfo(GameType.RITOPLUS, null));
                case "wildrift" -> handleWildrift(lastKnownPresence.getGameInfo(GameType.WILDRIFT, null));
                case "bacon" -> handleLOR(lastKnownPresence.getGameInfo(GameType.BACON, null));
            }
        }
        if (lastKnownPresence instanceof MobilePresence) handleRiotMobile(null);
        if (lastKnownPresence instanceof OfflinePresence) this.status = "";
    }

    public ConnectionStatus getConnectionStatus() {
        return lastKnownPresence != null ? lastKnownPresence.getConnectionStatus() : ConnectionStatus.OFFLINE;
    }

    public void executeOnClick(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graphics2D = (Graphics2D) g;
        Dimension dimension = getSize();

        int width = getHeight() / 3;
        int height = getHeight() / 3;
        int computedX = getWidth() - width - getWidth() / 20;
        int computedY = getHeight() / 2 - height / 2;

        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int consumedSpace = dimension.width - computedX;
        if (status != null) PaintHelper.drawShadowText(
                graphics2D,
                statusFont,
                status,
                new Rectangle(3, 0, dimension.width - consumedSpace, getHeight() / 6),
                LTextAlign.LEFT,
                color
        );

        graphics2D.setColor(color);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.fill(new RoundRectangle2D.Float(computedX, computedY, width, height, 360, 360));

        if (counter == 0) return;

        FontMetrics metrics = graphics2D.getFontMetrics();
        int notificationWidth = metrics.stringWidth(String.valueOf(counter));
        int widthMargin = 8, heightMargin = 2, notificationOffset = 30;
        int computedWidth = notificationWidth + (widthMargin << 1);
        int computedHeight = metrics.getAscent() + (heightMargin << 1);
        int rectangleX = dimension.width - widthMargin - computedWidth - notificationOffset;
        int rectangleY = (dimension.height >> 1) - (computedHeight >> 1);

        graphics2D.setColor(Color.BLACK);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.fill(new RoundRectangle2D.Float(rectangleX + 1, rectangleY + 1, computedWidth, computedHeight, 5, 5));

        graphics2D.setColor(new Color(255, 175, 79));
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.fill(new RoundRectangle2D.Float(rectangleX, rectangleY, computedWidth, computedHeight, 5, 5));

        drawHighlightedText(graphics2D, new Rectangle(rectangleX, rectangleY, computedWidth, computedHeight), String.valueOf(counter));
    }

    private void drawHighlightedText(Graphics2D g, Rectangle rectangle, String text) {
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text);
        int x = rectangle.x + (rectangle.width >> 1) - (width >> 1);
        int y = rectangle.y + (rectangle.height >> 1) + (metrics.getAscent() >> 1) - 1;
        g.setColor(Color.BLACK);
        g.drawString(text, x + 1, y + 1);
        g.setColor(ColorPalette.textColor);
        g.drawString(text, x, y);
    }

    public void increment() {
        this.counter += 1;
        this.repaint();
    }

    public void opened() {
        this.counter = 0;
        this.repaint();
    }

    public void clickEvent(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            LeagueClientUI.service.execute(runnable);
            this.repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            JPopupMenu menu = new JPopupMenu();
            if (lastKnownPresence != null && (lastKnownPresence instanceof BasicPresence presence)) {
                LOL game = presence.getGameInfo(GameType.LEAGUE_OF_LEGENDS, null);
                if (game != null) {
                    game.getPty().ifPresent(pty -> {
                        JMenuItem join = new JMenuItem(new AbstractAction("Join Lobby") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                LeagueClient client = leagueClientUI.getLeagueClient();
                                try {
                                    client.getLedge().getParties().role(pty.getPartyId(), PartyRole.MEMBER);
                                    leagueClientUI.getLayoutManager().showClientComponent("play");
                                    game.getPresenceInfo().ifPresent(info -> {
                                        if (info.getGameMode().toLowerCase().contains("tft")) {
                                            leagueClientUI.getLayoutManager().getQueue().getTftLobby().actionPerformed(null);
                                        } else {
                                            leagueClientUI.getLayoutManager().getQueue().getDraftLobby().actionPerformed(null);
                                        }
                                    });
                                } catch (IOException ex) {
                                    Logger.error(ex);
                                }
                            }
                        });
                        menu.add(join);
                    });
                }
            }
            JMenuItem invite = new JMenuItem(new AbstractAction("Invite Friend") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LedgeEndpoint ledges = leagueClientUI.getLeagueClient().getLedge();
                    SummonerLedge summonerLedge = ledges.getSummoner();
                    PartiesLedge partiesLedge = ledges.getParties();
                    try {
                        Summoner summoner = summonerLedge.resolveSummonerByPUUD(friend.getPUUID());
                        partiesLedge.invite(summoner.getPUUID());
                    } catch (IOException | PartyException ex) {
                        Logger.error(ex);
                    }
                }
            });
            JMenuItem remove = new JMenuItem(new AbstractAction("Remove Friend") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    xmppClient.removeFriend(friend.getJID());
                }
            });
            JMenuItem block = new JMenuItem(new AbstractAction("Block User") {
                @Override
                public void actionPerformed(ActionEvent e) {

                    xmppClient.blockUser(friend.getJID());
                    xmppClient.removeFriend(friend.getJID());
                }
            });
            JMenuItem close = new JMenuItem(new AbstractAction("Close Menu") {
                @Override
                public void actionPerformed(ActionEvent e) {

                }
            });
            menu.add(invite);
            menu.add(remove);
            menu.add(block);
            menu.add(close);
            menu.show(this, e.getX(), e.getY());
        }
    }
}
