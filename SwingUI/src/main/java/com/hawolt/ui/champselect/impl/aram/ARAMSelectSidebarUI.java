package com.hawolt.ui.champselect.impl.aram;

import com.hawolt.async.ExecutorManager;
import com.hawolt.ui.champselect.data.ChampSelectMember;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.generic.impl.ChampSelectMemberElement;
import com.hawolt.ui.champselect.generic.impl.ChampSelectSidebarUI;
import com.hawolt.ui.generic.themes.ColorPalette;

import java.awt.*;
import java.util.concurrent.ExecutorService;

/**
 * Created: 31/08/2023 21:10
 * Author: Twitter @hawolt
 **/

public class ARAMSelectSidebarUI extends ChampSelectSidebarUI {

    public ARAMSelectSidebarUI(ChampSelectTeam team) {
        super(team);
    }

    @Override
    public void init() {
        if (context == null) return;
        this.map.clear();
        this.display.removeAll();
        this.type = getChampSelectTeamType();
        ChampSelectMember[] members = get(type);
        this.display.setBackground(ColorPalette.backgroundColor);
        if (members.length != 0) {
            populate(members);
        } else {
            this.display.setLayout(new GridLayout(5, 0, 0, 5));
            for (int i = 0; i < 5; i++) {
                ExecutorService loader = ExecutorManager.getService("name-loader");
                ChampSelectMemberElement element = new ChampSelectMemberElement(type, team, null);
                map.put(i, element);
                element.setIndex(context);
                loader.execute(element);
                this.display.add(element);
            }
        }
        revalidate();
    }
}
