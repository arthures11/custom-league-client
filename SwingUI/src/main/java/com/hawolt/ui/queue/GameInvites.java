package com.hawolt.ui.queue;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.resources.ledge.parties.objects.AvailableParty;
import com.hawolt.client.resources.ledge.parties.objects.PartiesRegistration;
import com.hawolt.client.resources.ledge.parties.objects.Party;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Created: 21/08/2023 22:32
 * Author: Twitter @hawolt
 **/

public class GameInvites extends ChildUIComponent implements IServiceMessageListener<RiotMessageServiceMessage> {
    private final LeagueClientUI leagueClientUI;

    public GameInvites(LeagueClientUI leagueClientUI) {
        super(new GridLayout(0, 1, 0, 5));
        this.setBorder(new EmptyBorder(5, 0, 0, 0));
        this.leagueClientUI = leagueClientUI;
        this.setBackground(Color.GRAY);
        this.setVisible(false);
        this.leagueClientUI.getLeagueClient()
                .getRMSClient()
                .getHandler()
                .addMessageServiceListener(MessageService.PARTIES, this);
    }


    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        JSONObject payload = riotMessageServiceMessage.getPayload().getPayload();
        if (payload.isNull("player")) return;
        PartiesRegistration registration = new PartiesRegistration(payload.getJSONObject("player"));
        List<Party> list = registration.getParties().stream().filter(party -> party.getRole().equals("INVITED")).toList();
        setVisible(!list.isEmpty());
        removeAll();
        SummonerLedge ledge = leagueClientUI.getLeagueClient().getLedge().getSummoner();
        try {
            for (Party party : list) {
                AvailableParty availableParty = (AvailableParty) party;
                Summoner summoner = ledge.resolveSummonerByPUUD(availableParty.getInvitedByPUUID());
                add(new GameInvite(leagueClientUI, summoner, party));
            }
        } catch (IOException e) {
            Logger.error(e);
        }
        revalidate();
    }
}
