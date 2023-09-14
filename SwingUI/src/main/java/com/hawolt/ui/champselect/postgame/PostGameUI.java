package com.hawolt.ui.champselect.postgame;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.resources.ledge.leagues.objects.LeagueNotification;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.layout.LayoutComponent;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

/**
 * Created: 11/09/2023 20:58
 * Author: Twitter @hawolt
 **/

public class PostGameUI extends ChildUIComponent implements ActionListener {
    private final LeagueClientUI leagueClientUI;
    private final PostGameHeader header;
    private final PostGameScoreboard scoreboard;
    private final LFlatButton close;

    public PostGameUI(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(header = new PostGameHeader(), BorderLayout.NORTH);
        this.add(scoreboard = new PostGameScoreboard(), BorderLayout.CENTER);
        this.add(close = new LFlatButton("Play Again", LTextAlign.CENTER, HighlightType.COMPONENT), BorderLayout.SOUTH);
        this.close.addActionListener(this);
        this.leagueClientUI = leagueClientUI;
    }

    public void build(IResponse response, List<LeagueNotification> notifications) {
        build(response.asString(), notifications);
    }

    public void build(String response, List<LeagueNotification> notifications) {
        JSONObject data = new JSONObject(response);
        header.update(data, notifications);
        scoreboard.update(data);
        /*JOIN POST GAME CHAT
        String xmppRoomName = data.getString("roomName");
        String xmppRoomPassword = data.getString("roomPassword");
        PartyMucJwtDto mucJwtDto = new PartyMucJwtDto(data.getJSONObject("mucJwtDto"));
        client.getXMPPClient().joinProtectedMuc(xmppRoomName, mucJwtDto.getDomain(), xmppRoomPassword);
        */
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.leagueClientUI.getHeader().selectAndShowComponent(LayoutComponent.PLAY);
        LeagueClientUI.service.execute(() -> {
            try {
                this.leagueClientUI.getLeagueClient().getLedge().getParties().ready();
                this.leagueClientUI.getLayoutManager()
                        .getChampSelectUI()
                        .getChampSelect()
                        .getChampSelectDataContext()
                        .getPUUIDResolver()
                        .clear();
            } catch (IOException ex) {
                Logger.error(ex);
            }
        });
    }
}
