package com.hawolt.ui.champselect.impl;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.client.resources.communitydragon.spell.Spell;
import com.hawolt.client.resources.ledge.teambuilder.objects.MatchContext;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.service.impl.TeamBuilderService;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.ui.champselect.AbstractRenderInstance;
import com.hawolt.ui.champselect.data.ActionObject;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.data.GameType;
import com.hawolt.ui.champselect.generic.ChampSelectRuneSelection;
import com.hawolt.ui.champselect.generic.impl.*;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.objects.conversation.history.impl.IncomingMessage;
import com.hawolt.xmpp.event.objects.presence.impl.JoinMucPresence;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;

/**
 * Created: 29/08/2023 17:04
 * Author: Twitter @hawolt
 **/

public abstract class MatchmadeRenderInstance extends AbstractRenderInstance implements ActionListener {
    protected final ChampSelectSidebarUI teamOne, teamTwo;
    protected final ChampSelectGameSettingUI settingUI;
    protected final ChampSelectHeaderUI headerUI;
    protected final ChampSelectCenterUI centerUI;
    protected final ChampSelectChatUI chatUI;
    protected int selectedChampionId, bannedChampionId;

    public MatchmadeRenderInstance(ChampSelectType... supportedTypes) {
        this.component.add(centerUI = getCenterUI(this, supportedTypes), BorderLayout.CENTER);
        this.centerUI.add(teamTwo = getSidebarUI(ChampSelectTeam.PURPLE), BorderLayout.EAST);
        this.centerUI.add(teamOne = getSidebarUI(ChampSelectTeam.BLUE), BorderLayout.WEST);
        this.centerUI.getNorthernChild().add(headerUI = getHeaderUI(), BorderLayout.CENTER);
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        this.centerUI.getSouthernChild().add(component, BorderLayout.NORTH);
        component.add(new ChampSelectDebugUI(), BorderLayout.NORTH);
        component.add(settingUI = new ChampSelectGameSettingUI(getAllowedSummonerSpells()), BorderLayout.CENTER);
        this.centerUI.getSouthernChild().add(chatUI = new ChampSelectChatUI(), BorderLayout.CENTER);
        this.build();
    }

    protected abstract ChampSelectCenterUI getCenterUI(AbstractRenderInstance instance, ChampSelectType... supportedTypes);

    protected abstract ChampSelectSidebarUI getSidebarUI(ChampSelectTeam team);

    protected abstract ChampSelectHeaderUI getHeaderUI();

    protected abstract Integer[] getAllowedSummonerSpells();

    @Override
    public void push(IncomingMessage incomingMessage) {
        if (chatUI == null) return;
        chatUI.push(incomingMessage);
    }

    @Override
    public void push(JoinMucPresence presence) {
        if (chatUI == null) return;
        chatUI.push(presence);
    }

    @Override
    public void init() {
        super.init();
        int targetQueueId = context.getChampSelectSettingsContext().getQueueId();
        int[] supportedQueueIds = getSupportedQueueIds();
        for (int supportedQueueId : supportedQueueIds) {
            if (supportedQueueId == targetQueueId) {
                LeagueClientUI.service.execute(() -> {
                    LeagueClient client = context.getChampSelectDataContext().getLeagueClient();
                    if (client == null) return;
                    MatchContext context = client.getCachedValue(CacheType.MATCH_CONTEXT);
                    if (context == null) return;
                    chatUI.setMatchContext(context);
                    VirtualRiotXMPPClient xmppClient = client.getXMPPClient();
                    xmppClient.joinUnprotectedMuc(context.getPayload().getChatRoomName(), context.getPayload().getTargetRegion());
                });
            }
        }
    }

    private void build() {
        settingUI.getSubmitButton().addActionListener(this);
        settingUI.getDodgeButton().addActionListener(this);
        settingUI.getRuneButton().addActionListener(this);
        settingUI.getSpellOne().addActionListener(this);
        settingUI.getSpellTwo().addActionListener(this);
    }

    public ChampSelectSidebarUI getTeamOne() {
        return teamOne;
    }

    public ChampSelectSidebarUI getTeamTwo() {
        return teamTwo;
    }

    @Override
    public void onSummonerSubmission(Spell selectedSpellOne, Spell selectedSpellTwo) {
        try {
            if (context == null) return;
            LeagueRtmpClient rtmpClient = context.getChampSelectDataContext().getLeagueClient().getRTMPClient();
            TeamBuilderService teamBuilderService = rtmpClient.getTeamBuilderService();
            teamBuilderService.selectSpellsBlocking(selectedSpellOne.getId(), selectedSpellTwo.getId());
        } catch (Exception e) {
            Logger.error("Unable to submit selection");
            Logger.error(e);
        }
    }

    protected void onChoice(ActionObject actionObject, int championId, boolean completed) {
        Logger.info("[champ-select] indicate {} as {}", championId, completed);
        Logger.info("[champ-select] {}", actionObject);
        try {
            if (context == null) return;
            LeagueRtmpClient rtmpClient = context.getChampSelectDataContext().getLeagueClient().getRTMPClient();
            TeamBuilderService teamBuilderService = rtmpClient.getTeamBuilderService();
            teamBuilderService.updateActionV1Asynchronous(new PacketCallback() {
                @Override
                public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) {
                    Logger.info("OBJECT: {}", typedObject);
                }
            }, actionObject.getActionId(), championId, completed);
        } catch (Exception e) {
            Logger.error("Unable to submit selection");
            Logger.error(e);
        }
    }

    @Override
    public void onChoiceSubmission(ChampSelectType type, int championId, boolean completed) {
        if (context == null) return;
        Optional<ActionObject> optional = switch (type) {
            case PICK -> context.getChampSelectUtilityContext().getOwnPickPhase();
            case BAN -> context.getChampSelectUtilityContext().getOwnBanPhase();
        };
        optional.ifPresentOrElse(phase -> {
            Logger.info("{}, {}, {}, {}", type, phase.getActionId(), championId, completed);
            onChoice(phase, championId, completed);
        }, () -> {
            Logger.info("{}, {}, {}", type, championId, completed);
            Logger.error("Unable to find own phase");
        });
    }

    @Override
    public void onChoice(ChampSelectSelectionElement element) {
        super.onChoice(element);
        int championId = element.getChampionId();
        switch (element.getType()) {
            case PICK -> this.selectedChampionId = championId;
            case BAN -> this.bannedChampionId = championId;
        }
        this.onChoiceSubmission(element.getType(), championId, false);
    }

    @Override
    public void onSwapChoice(ChampSelectBenchElement element) {
        int championId = element.getChampionId();
        if (championId == -1) return;
        LeagueRtmpClient rtmpClient = context.getChampSelectDataContext().getLeagueClient().getRTMPClient();
        TeamBuilderService teamBuilderService = rtmpClient.getTeamBuilderService();
        try {
            teamBuilderService.championBenchSwapV1Asynchronous(new PacketCallback() {
                @Override
                public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) {
                    Logger.info("OBJECT: {}", typedObject);
                }
            }, championId);
        } catch (IOException e) {
            Logger.error("Unable to bench swap");
            Logger.error(e);
        }
    }

    @Override
    public void invokeChampionFilter(String champion) {
        for (ChampSelectType type : ChampSelectType.values()) {
            if (!centerUI.isConfigured(type)) continue;
            this.centerUI.getSelectionUI(type).filter(champion);
        }
    }

    @Override
    public void setGlobalRunePanel(ChampSelectRuneSelection selection) {
        this.centerUI.setRuneSelection("runes", selection);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Dodge" -> {
                int result = JOptionPane.showConfirmDialog(
                        Frame.getFrames()[0],
                        "Dodging will result in loss of LP, are you sure?",
                        "Warning",
                        JOptionPane.YES_NO_OPTION
                );
                if (result == JOptionPane.YES_OPTION) dodge(GameType.CLASSIC);
            }
            case "Submit Choice" -> {
                int size = context.getChampSelectSettingsContext().getActionSetMapping().size();
                if (size > 1) {
                    if (context.getChampSelectSettingsContext().getCurrentActionSetIndex() == 0) {
                        onChoiceSubmission(ChampSelectType.BAN, bannedChampionId, true);
                    } else {
                        onChoiceSubmission(ChampSelectType.PICK, selectedChampionId, true);
                    }
                } else {
                    onChoiceSubmission(ChampSelectType.PICK, selectedChampionId, true);
                }
            }
            case "Rune Page" -> {
                centerUI.toggleCard("runes");
            }
            case "comboBoxChanged" -> {
                onSummonerSubmission(
                        settingUI.getSelectedSpellOne(),
                        settingUI.getSelectedSpellTwo()
                );
            }
        }
    }
}
