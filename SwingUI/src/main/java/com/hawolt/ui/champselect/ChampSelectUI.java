package com.hawolt.ui.champselect;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.client.resources.ledge.leagues.objects.LeagueLedgeNotifications;
import com.hawolt.client.resources.ledge.leagues.objects.LeagueNotification;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.http.layer.IResponse;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.impl.payload.RiotMessageMessagePayload;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.context.impl.ChampSelect;
import com.hawolt.ui.champselect.impl.aram.ARAMChampSelectUI;
import com.hawolt.ui.champselect.impl.blank.BlankChampSelectUI;
import com.hawolt.ui.champselect.impl.blind.BlindChampSelectUI;
import com.hawolt.ui.champselect.impl.draft.DraftChampSelectUI;
import com.hawolt.ui.champselect.postgame.PostGameUI;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.layout.LayoutComponent;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created: 29/08/2023 16:59
 * Author: Twitter @hawolt
 **/

public class ChampSelectUI extends ChildUIComponent implements IServiceMessageListener<RiotMessageServiceMessage> {
    private final Map<Integer, String> QUEUE_RENDERER_MAPPING = new HashMap<>();
    private final List<AbstractRenderInstance> instances = new ArrayList<>();
    private final CardLayout layout = new CardLayout();
    private final JComponent main = new ChildUIComponent(layout);
    private final ChampSelect champSelect;
    private LeagueClientUI leagueClientUI;
    private LeagueClient leagueClient;
    private PostGameUI postGameUI;

    public ChampSelectUI(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());
        this.add(main, BorderLayout.CENTER);
        if (leagueClientUI != null) {
            this.leagueClientUI = leagueClientUI;
            this.postGameUI = new PostGameUI(leagueClientUI);
            this.leagueClient = leagueClientUI.getLeagueClient();
            this.champSelect = new ChampSelect(this);
            this.leagueClient.getRMSClient().getHandler().addMessageServiceListener(MessageService.LOL_PLATFORM, this);
            this.leagueClient.getRTMPClient().addDefaultCallback(champSelect.getChampSelectDataContext().getPacketCallback());
            this.main.add("summary", postGameUI);
        } else {
            this.champSelect = new ChampSelect(this);
        }
        this.addRenderInstance(BlankChampSelectUI.INSTANCE);
        this.addRenderInstance(DraftChampSelectUI.INSTANCE);
        this.addRenderInstance(BlindChampSelectUI.INSTANCE);
        this.addRenderInstance(ARAMChampSelectUI.INSTANCE);
        this.showBlankPanel();
    }

    public ChampSelectUI() {
        this(null);
    }

    public void showPostGamePanel() {
        this.layout.show(main, "summary");
    }

    public void showBlankPanel() {
        this.layout.show(main, "blank");
    }

    public PostGameUI getPostGameUI() {
        return postGameUI;
    }

    private void addRenderInstance(AbstractRenderInstance instance) {
        instance.setGlobalRunePanel(champSelect.getChampSelectInterfaceContext().getRuneSelectionPanel());
        int[] queueIds = instance.getSupportedQueueIds();
        for (int id : queueIds) {
            Logger.info("[champ-select] register queueId:{} as '{}'", id, instance.getCardName());
            QUEUE_RENDERER_MAPPING.put(id, instance.getCardName());
        }
        this.instances.add(instance);
        this.main.add(instance.getCardName(), instance);
    }

    public void update(ChampSelectContext context) {
        int initialCounter;
        if (leagueClient != null) {
            MatchContext matchContext = leagueClient.getCachedValue(CacheType.MATCH_CONTEXT);
            initialCounter = matchContext.getPayload().getCounter() + 1;
        } else {
            initialCounter = 5;
        }
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        if (settingsContext.getCounter() == initialCounter) {
            String card = QUEUE_RENDERER_MAPPING.getOrDefault(settingsContext.getQueueId(), "blank");
            Logger.info("[champ-select] switch to card {}", card);
            this.layout.show(main, card);
            if (leagueClientUI != null) {
                leagueClientUI.getHeader().selectAndShowComponent(LayoutComponent.CHAMPSELECT);
            }
        }
        for (AbstractRenderInstance instance : instances) {
            instance.delegate(context, initialCounter);
        }
        this.repaint();
    }

    public ChampSelect getChampSelect() {
        return champSelect;
    }

    public LeagueClient getLeagueClient() {
        return leagueClient;
    }

    public LeagueClientUI getLeagueClientUI() {
        return leagueClientUI;
    }

    public List<AbstractRenderInstance> getInstances() {
        return instances;
    }

    @Override
    public void onMessage(RiotMessageServiceMessage unspecified) throws Exception {
        RiotMessageMessagePayload base = unspecified.getPayload();
        if (!base.getResource().endsWith("lol-platform/v1/gsm/stats")) return;
        JSONObject payload = base.getPayload();
        long gameId = payload.getLong("gameId");
        LeagueLedgeNotifications ledgeNotifications = leagueClient.getLedge().getLeague().getNotifications();
        List<LeagueNotification> leagueNotifications = ledgeNotifications.getLeagueNotifications();
        IResponse response = leagueClient.getLedge().getUnclassified().getEndOfGame(gameId);
        postGameUI.build(response, leagueNotifications);
        ChampSelectUI.this.showPostGamePanel();
        ChampSelectUI.this.leagueClientUI.getHeader().selectAndShowComponent(LayoutComponent.CHAMPSELECT);
        boolean processed = leagueClient.getLedge().getChallenge().notify(gameId);
        if (!processed) {
            Logger.error("unable to submit game {} as processed", gameId);
        } else {
            Logger.info("submitting game {} as processed", gameId);
        }
    }
}
