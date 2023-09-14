package com.hawolt.ui.queue;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.resources.Loadout;
import com.hawolt.client.resources.ledge.parties.objects.data.TFTLegend;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LComboBox;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created: 11/08/2023 23:00
 * Author: Twitter @hawolt
 **/

public class TFTQueueLobby extends QueueLobby {


    public TFTQueueLobby(LeagueClientUI leagueClientUI, Container parent, CardLayout layout, QueueWindow queueWindow) {
        super(leagueClientUI, parent, layout, queueWindow);
    }


    @Override
    public SummonerComponent getSummonerComponentAt(int id) {
        int index;
        if (id == 0) index = 1;
        else if (id == 1) index = 2;
        else if (id == 2) index = 0;
        else if (id == 3) index = 3;
        else if (id == 4) index = 5;
        else if (id == 5) index = 6;
        else if (id == 6) index = 4;
        else index = 7;
        return (SummonerComponent) grid.getComponent(index);
    }


    @Override
    protected void createSpecificComponents(ChildUIComponent component) {
        Loadout loadout = new Loadout(leagueClientUI.getLeagueClient());
        LComboBox<TFTLegend> legend = new LComboBox<>(TFTLegend.values());
        legend.addActionListener(e -> {
            AtomicInteger atomicItemId = new AtomicInteger();
            switch (legend.getItemAt(legend.getSelectedIndex())) {
                case PORO -> atomicItemId.set(1);
                case VEIGAR -> atomicItemId.set(15);
                case TAHM -> atomicItemId.set(16);
                case VLADIMIR -> atomicItemId.set(17);
                case EZREAL -> atomicItemId.set(18);
                case DRAVEN -> atomicItemId.set(19);
                case TF -> atomicItemId.set(20);
                case URF -> atomicItemId.set(21);
                case ORNN -> atomicItemId.set(22);
                case CATILYN -> atomicItemId.set(23);
                case PENGU -> atomicItemId.set(24);
                case YI -> atomicItemId.set(25);
                case LEE -> atomicItemId.set(26);
                case AURELION -> atomicItemId.set(27);
                case BARD -> atomicItemId.set(28);
            }
            LeagueClientUI.service.execute(() -> {
                try {
                    loadout.setLegend(atomicItemId.get(), leagueClientUI.getLeagueClient().getLedge().getInventoryService().getLegendInstanceId(atomicItemId.get()));
                } catch (Exception ignored) {
                }
            });
        });
        AtomicInteger index = new AtomicInteger();
        try {
            switch (loadout.getLegend()) {
                case 1 -> index.set(0);
                case 15 -> index.set(1);
                case 16 -> index.set(2);
                case 17 -> index.set(3);
                case 18 -> index.set(4);
                case 19 -> index.set(5);
                case 20 -> index.set(6);
                case 21 -> index.set(7);
                case 22 -> index.set(8);
                case 23 -> index.set(9);
                case 24 -> index.set(10);
                case 25 -> index.set(11);
                case 26 -> index.set(12);
                case 27 -> index.set(13);
                case 28 -> index.set(14);
            }
        } catch (IOException e) {
            Logger.error(e);
            index.set(1);
        }
        legend.setSelectedIndex(index.get());
        try {
            loadout.getLoadout();
        } catch (IOException e) {
            Logger.error(e);
        }
        component.add(legend, BorderLayout.SOUTH);
    }

    @Override
    protected void createGrid(ChildUIComponent component) {
        grid = new ChildUIComponent(new GridLayout(2, 4));
        for (int i = 0; i < 8; i++) grid.add(new SummonerComponent());
        grid.setBackground(Color.YELLOW);
        component.add(grid, BorderLayout.CENTER);
    }


}
