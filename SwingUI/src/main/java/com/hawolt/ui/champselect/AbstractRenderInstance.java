package com.hawolt.ui.champselect;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.ui.champselect.context.ChampSelectContext;
import com.hawolt.ui.champselect.context.ChampSelectDataContext;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.data.GameType;
import com.hawolt.ui.champselect.generic.ChampSelectRuneSelection;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.generic.impl.ChampSelectChoice;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSelectionElement;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;
import com.hawolt.xmpp.event.objects.presence.impl.JoinMucPresence;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created: 29/08/2023 18:04
 * Author: Twitter @hawolt
 **/

public abstract class AbstractRenderInstance extends ChampSelectUIComponent implements ChampSelectRenderer, ChampSelectChoice {

    protected final ChildUIComponent component = new ChildUIComponent(new BorderLayout());
    private final Map<ChampSelectType, ChampSelectSelectionElement> map = new ConcurrentHashMap<>();

    public AbstractRenderInstance() {
        this.setLayout(new BorderLayout());
        this.add(component, BorderLayout.CENTER);
    }

    public abstract void push(IncomingMessage incomingMessage);

    public abstract void push(JoinMucPresence presence);

    protected abstract void stopChampSelect();

    public abstract void invokeChampionFilter(String champion);

    public abstract void setGlobalRunePanel(ChampSelectRuneSelection selection);

    @Override
    public void init() {
        if (context == null) return;
        for (ChampSelectType type : map.keySet()) {
            ChampSelectSelectionElement element = map.get(type);
            element.setSelected(false);
            element.repaint();
        }
        map.clear();
    }

    @Override
    public void onChoice(ChampSelectSelectionElement element) {
        if (context == null || element == null) return;
        if (map.containsKey(element.getType())) {
            ChampSelectSelectionElement champSelectSelectionElement = map.get(element.getType());
            if (champSelectSelectionElement == null) return;
            champSelectSelectionElement.setSelected(false);
            champSelectSelectionElement.repaint();
        }
        map.put(element.getType(), element);
    }

    public void dodge(GameType type) {
        if (context == null) return;
        LeagueClientUI.service.execute(() -> {
            ChampSelectDataContext dataContext = context.getChampSelectDataContext();
            LeagueClient client = dataContext.getLeagueClient();
            LeagueRtmpClient rtmpClient = client.getRTMPClient();
            LeagueClientUI leagueClientUI = context.getChampSelectInterfaceContext().getLeagueClientUI();
            try {
                switch (type) {
                    case CLASSIC ->
                            rtmpClient.getTeamBuilderService().quitGameV2Asynchronous(dataContext.getPacketCallback());
                    case CUSTOM -> Logger.debug("currently not supported");
                }
                leagueClientUI.getChatSidebar().getEssentials().disableQueueState();
                context.getChampSelectUtilityContext().quitChampSelect();
                revalidate();
            } catch (IOException e) {
                Logger.error("failed to quit game");
            }
        });
    }

    public void delegate(ChampSelectContext context, int initialCounter) {
        this.assign(this, context);
        this.configure(context);
        this.execute(initialCounter);
        this.update(this, initialCounter);
    }

    private void assign(JComponent parent, ChampSelectContext context) {
        Component[] components = parent.getComponents();
        for (Component component : components) {
            if (component == null) continue;
            if ((component instanceof ChampSelectUIComponent champSelectUIComponent)) {
                champSelectUIComponent.configure(context);
            }
            if (component instanceof JComponent child) assign(child, context);
        }
    }

    private void update(JComponent parent, int initialCounter) {
        Component[] components = parent.getComponents();
        for (Component component : components) {
            if (component == null) continue;
            if (component instanceof JComponent child) update(child, initialCounter);
            if ((component instanceof ChampSelectUIComponent champSelectUIComponent)) {
                champSelectUIComponent.execute(initialCounter);
            }
        }
    }
}
