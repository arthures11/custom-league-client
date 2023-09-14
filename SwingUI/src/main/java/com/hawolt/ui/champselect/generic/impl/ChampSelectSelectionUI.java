package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.LeagueClientUI;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.communitydragon.champion.Champion;
import com.hawolt.client.resources.communitydragon.champion.ChampionIndex;
import com.hawolt.client.resources.communitydragon.champion.ChampionSource;
import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.data.ChampSelectType;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.generic.component.LScrollPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created: 29/08/2023 19:47
 * Author: Twitter @hawolt
 **/

public class ChampSelectSelectionUI extends ChampSelectUIComponent {
    private static final String IMAGE_ICON_BASE = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-icons/%s.png";
    private final Map<Integer, ChampSelectSelectionElement> map = new HashMap<>();
    private final ChildUIComponent component = new ChildUIComponent();
    private final ChampSelectChoice callback;
    private final ChampSelectType type;
    private int[] championsAvailableAsChoice = new int[0];
    private String filter = "";

    public ChampSelectSelectionUI(ChampSelectType type, ChampSelectChoice callback) {
        ColorPalette.addThemeListener(this);
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        LScrollPane scrollPane = new LScrollPane(component);
        component.setBackground(ColorPalette.backgroundColor);
        component.setLayout(new GridLayout(0, 5, 5, 5));
        component.setBorder(new EmptyBorder(5, 0, 0, 0));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        this.add(scrollPane, BorderLayout.CENTER);
        this.callback = callback;
        this.type = type;
    }

    private void configure() {
        ChampionIndex championIndex = ChampionSource.CHAMPION_SOURCE_INSTANCE.get();
        Integer[] boxed = IntStream.of(championsAvailableAsChoice).boxed().toArray(Integer[]::new);
        LeagueClientUI.service.execute(() -> {
            Arrays.sort(boxed, (id1, id2) -> {
                String name1 = championIndex.getChampion(id1).getName();
                String name2 = championIndex.getChampion(id2).getName();
                return name1.compareTo(name2);
            });
            component.removeAll();
            for (int championId : boxed) {
                Champion champion = championIndex.getChampion(championId);
                if (!champion.getName().toLowerCase().contains(filter)) continue;
                if (!map.containsKey(championId)) {
                    ChampSelectSelectionElement element = new ChampSelectSelectionElement(callback, type, championId, champion.getName());
                    ResourceLoader.loadResource(String.format(IMAGE_ICON_BASE, championId), element);
                    map.put(championId, element);
                }
                ChampSelectSelectionElement element = map.get(championId);
                component.add(element);
            }
            this.revalidate();
            this.repaint();
        });
    }

    @Override
    public void update() {
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        int[] championsAvailableAsChoice = switch (type) {
            case PICK -> settingsContext.getChampionsAvailableForPick();
            case BAN -> settingsContext.getChampionsAvailableForBan();
        };
        if (this.championsAvailableAsChoice.length == championsAvailableAsChoice.length) return;
        this.championsAvailableAsChoice = championsAvailableAsChoice;
        this.configure();
    }

    public void filter(String champion) {
        if (context == null) return;
        this.filter = champion.toLowerCase();
        this.configure();
    }
}
