package com.hawolt.async.rms;

import com.hawolt.LeagueClientUI;
import com.hawolt.generic.data.Platform;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.util.other.Launcher;

/**
 * Created: 11/08/2023 18:01
 * Author: Twitter @hawolt
 **/

public class GameStartListener implements IServiceMessageListener<RiotMessageServiceMessage> {
    private final LeagueClientUI leagueClientUI;
    private final Platform platform;

    public GameStartListener(LeagueClientUI leagueClientUI) {
        this.platform = leagueClientUI.getLeagueClient().getPlayerPlatform();
        this.leagueClientUI = leagueClientUI;
    }

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        boolean gameStart = riotMessageServiceMessage.getPayload().getResource().endsWith("player-credentials-update");
        if (!gameStart) return;
        Launcher.launch(leagueClientUI.getSettingService(), platform, riotMessageServiceMessage.getPayload().getPayload());
        leagueClientUI.getChatSidebar().getEssentials().disableQueueState();
        leagueClientUI.getLayoutManager().getChampSelectUI().showBlankPanel();
    }

}
