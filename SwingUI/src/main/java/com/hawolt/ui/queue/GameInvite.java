package com.hawolt.ui.queue;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.parties.objects.AvailableParty;
import com.hawolt.client.resources.ledge.parties.objects.Party;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyRole;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created: 21/08/2023 23:00
 * Author: Twitter @hawolt
 **/

public class GameInvite extends ChildUIComponent implements ActionListener {
    private final LeagueClientUI leagueClientUI;
    private final Party party;

    public GameInvite(LeagueClientUI leagueClientUI, Summoner summoner, Party party) {
        super(new BorderLayout());
        this.setBackground(Color.GRAY);
        this.leagueClientUI = leagueClientUI;
        setBackground(Color.GRAY);
        JLabel name = new JLabel(summoner.getName());
        name.setForeground(Color.WHITE);
        name.setBackground(Color.GRAY);
        add(name, BorderLayout.CENTER);
        JButton button = new JButton("Join");
        button.addActionListener(this);
        add(button, BorderLayout.EAST);
        this.party = party;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LeagueClient client = leagueClientUI.getLeagueClient();
        try {
            int queueId = ((AvailableParty) party).getPartyGameMode().getQueueId();
            client.getLedge().getParties().role(party.getPartyId(), PartyRole.MEMBER);
            leagueClientUI.getLayoutManager().showClientComponent("play");
            if (queueId == 1100 || queueId == 1090 || queueId == 1130 || queueId == 1160) {
                leagueClientUI.getLayoutManager().getQueue().getTftLobby().actionPerformed(null);
            } else {
                leagueClientUI.getLayoutManager().getQueue().getDraftLobby().actionPerformed(null);
            }
        } catch (IOException ex) {
            Logger.error(ex);
        }
    }
}
