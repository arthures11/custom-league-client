package com.hawolt.ui.queue;

import com.hawolt.LeagueClientUI;
import com.hawolt.async.ExecutorManager;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.*;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyAction;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyRole;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created: 11/08/2023 23:00
 * Author: Twitter @hawolt
 **/

abstract public class QueueLobby extends ChildUIComponent implements ActionListener, IServiceMessageListener<RiotMessageServiceMessage> {
    public final ScheduledExecutorService scheduler = ExecutorManager.getScheduledService("queue-resumer");

    public final LeagueClientUI leagueClientUI;
    public ScheduledFuture<?> future;
    public int queueId;
    public ChildUIComponent grid;
    public ChildUIComponent component = new ChildUIComponent(new BorderLayout());
    private CurrentParty party;
    private String puuid;

    public QueueLobby(LeagueClientUI leagueClientUI, Container parent, CardLayout layout, QueueWindow queueWindow) {
        super(new BorderLayout());

        createGrid(component);
        ChildUIComponent newButton = new ChildUIComponent(new BorderLayout());
        this.leagueClientUI = leagueClientUI;
        this.leagueClientUI.getLeagueClient().getRMSClient().getHandler().addMessageServiceListener(MessageService.PARTIES, this);

        ChildUIComponent top = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        LFlatButton close = new LFlatButton("Choose mode", LTextAlign.CENTER, HighlightType.COMPONENT);
        close.addActionListener(listener -> layout.show(parent, "modes"));

        LFlatButton invite = new LFlatButton("Invite another Summoner", LTextAlign.CENTER, HighlightType.COMPONENT);
        LFlatButton leave = new LFlatButton("Leave Party", LTextAlign.CENTER, HighlightType.COMPONENT);
        leave.addActionListener(listener -> {
            layout.show(parent, "modes");
            try {
                leagueClientUI.getLeagueClient().getLedge().getParties().role(PartyRole.DECLINED);
                queueId = 0;
                queueWindow.rebase();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        newButton.add(leave, BorderLayout.NORTH);
        close.addActionListener(listener -> {
            layout.show(parent, "modes");
        });
        newButton.add(close, BorderLayout.CENTER);
        add(newButton, BorderLayout.NORTH);
        invite.addActionListener(listener -> {
            String name = (String) JOptionPane.showInputDialog(
                    this,
                    "Summonername",
                    "Invite",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
            if (name == null) return;
            LedgeEndpoint ledges = leagueClientUI.getLeagueClient().getLedge();
            SummonerLedge summonerLedge = ledges.getSummoner();
            PartiesLedge partiesLedge = ledges.getParties();
            try {
                Summoner summoner = summonerLedge.resolveSummonerByName(name);
                partiesLedge.invite(summoner.getPUUID());
            } catch (IOException | PartyException e) {
                Logger.error(e);
            }
        });
        top.add(close);
        top.add(invite);
        component.add(top, BorderLayout.NORTH);
        LeagueClientUI.service.execute(() -> createSpecificComponents(component));

        add(component, BorderLayout.CENTER);
        ChildUIComponent bottom = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
        LFlatButton start = new LFlatButton("Start", LTextAlign.CENTER, HighlightType.COMPONENT);
        start.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
        start.setBackground(ColorPalette.buttonSelectionColor);
        start.setHighlightColor(ColorPalette.buttonSelectionAltColor);
        start.addActionListener(listener -> startQueue());
        LFlatButton stop = new LFlatButton("×", LTextAlign.CENTER, HighlightType.COMPONENT);
        stop.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
        stop.setBackground(ColorPalette.buttonSelectionColor);
        stop.setHighlightColor(ColorPalette.buttonSelectionAltColor);
        stop.addActionListener(listener -> {
            if (future != null) future.cancel(true);
            PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
            PartiesRegistration registration = partiesLedge.getCurrentRegistration();
            try {
                if (registration == null) return;
                partiesLedge.setQueueAction(PartyAction.STOP);
                leagueClientUI.getChatSidebar().getEssentials().disableQueueState();
            } catch (IOException e) {
                Logger.error(e);
            }
        });
        bottom.add(stop);
        bottom.add(start);
        add(bottom, BorderLayout.SOUTH);
    }

    protected abstract void createSpecificComponents(ChildUIComponent component);

    protected abstract void createGrid(ChildUIComponent component);

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        JSONObject payload = riotMessageServiceMessage.getPayload().getPayload();
        if (!payload.has("player") || payload.isNull("player")) return;
        PartiesRegistration registration = new PartiesRegistration(payload.getJSONObject("player"));
        puuid = registration.getPUUID();
        party = registration.getCurrentParty();
        List<PartyParticipant> partyParticipants = party.getPlayers();
        if (party == null) return;
        PartyRestriction restriction = party.getPartyRestriction();
        if (restriction != null) handleGatekeeperRestriction(restriction.getRestrictionList());
        partyParticipants.stream().filter(participant -> participant.getPUUID().equals(puuid)).findFirst().ifPresent(self -> {
            SummonerLedge summonerLedge = leagueClientUI.getLeagueClient().getLedge().getSummoner();
            try {
                getSummonerComponentAt(0).update(self, summonerLedge.resolveSummonerByPUUD(puuid));
                partyParticipants.remove(self);
                int memberPosition = 1;
                for (PartyParticipant participant : partyParticipants) {
                    Summoner summoner = summonerLedge.resolveSummonerByPUUD(participant.getPUUID());
                    if (participant.getRole().equals("MEMBER") || participant.getRole().equals("LEADER")) {
                        getSummonerComponentAt(memberPosition++).update(participant, summoner);
                    }
                }
                for (int i = memberPosition; i < party.getMaxPartySize(); i++) {
                    getSummonerComponentAt(i).update(null, null);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            revalidate();
        });
        if (party.getPartyGameMode() != null && queueId != party.getPartyGameMode().getQueueId()) {
            queueId = party.getPartyGameMode().getQueueId();
            try {
                leagueClientUI.getLeagueClient().getLedge().getParties().role(party.getPartyId(), PartyRole.MEMBER);
                if (queueId == 1100 || queueId == 1090 || queueId == 1130 || queueId == 1160) {
                    leagueClientUI.getLayoutManager().getQueue().getTftLobby().actionPerformed(null);
                } else {
                    leagueClientUI.getLayoutManager().getQueue().getDraftLobby().actionPerformed(null);
                }
                leagueClientUI.getLayoutManager().showClientComponent("play");
            } catch (IOException ex) {
                Logger.error(ex);
            }
        }
    }

    private void handleGatekeeperRestriction(List<GatekeeperRestriction> restrictions) {
        List<GatekeeperRestriction> sorted = restrictions.stream()
                .sorted(((o1, o2) -> Long.compare(o2.getRemainingMillis(), o1.getRemainingMillis())))
                .toList();
        GatekeeperRestriction gatekeeperRestriction = sorted.get(0);
        Logger.debug("Restriction: {}", gatekeeperRestriction);
        leagueClientUI.getChatSidebar().getEssentials().toggleQueueState(
                System.currentTimeMillis(),
                gatekeeperRestriction.getRemainingMillis(),
                true
        );
        future = scheduler.schedule(() -> {
            try {
                leagueClientUI.getLeagueClient().getLedge().getParties().resume();
            } catch (IOException e) {
                Logger.error(e);
            }
        }, gatekeeperRestriction.getRemainingMillis(), TimeUnit.MILLISECONDS);
    }

    abstract public SummonerComponent getSummonerComponentAt(int id);

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void startQueue() {
        PartiesLedge partiesLedge = leagueClientUI.getLeagueClient().getLedge().getParties();
        PartiesRegistration registration = partiesLedge.getCurrentRegistration();
        try {
            if (registration == null) partiesLedge.register();
            partiesLedge.ready();
            JSONObject response = partiesLedge.setQueueAction(PartyAction.START);
            List<GatekeeperRestriction> direct = response.has("errorCode") &&
                    "GATEKEEPER_RESTRICTED".equals(response.getString("errorCode")) ?
                    new PartyGatekeeper(response).getRestrictionList() : new ArrayList<>();
            CurrentParty party = partiesLedge.getOwnPlayer().getCurrentParty();
            PartyRestriction restriction = party.getPartyRestriction();
            List<GatekeeperRestriction> indirect = restriction != null ?
                    restriction.getRestrictionList() : new ArrayList<>();
            if (direct.isEmpty() && indirect.isEmpty()) return;
            handleGatekeeperRestriction(
                    Stream.of(direct, indirect).flatMap(Collection::stream).collect(Collectors.toList())
            );
        } catch (IOException e) {
            Logger.error(e);
        }
    }


}
