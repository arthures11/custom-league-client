package com.hawolt.ui.store;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.store.StoreLedge;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.ledge.store.objects.StoreSortProperty;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LTabbedPane;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Created: 09/08/2023 17:44
 * Author: Twitter @hawolt
 **/

public class StoreWindow extends ChildUIComponent implements Runnable {
    private final Map<InventoryType, List<StoreItem>> cache = new HashMap<>();
    private final LeagueClient client;

    private final LTabbedPane pane;

    public StoreWindow(LeagueClient client) {
        super(new BorderLayout());
        this.client = client;
        this.pane = new LTabbedPane();
        try {
            String jwt = client.getLedge().getInventoryService().getInventoryToken();
            JSONObject object = new JSONObject(new String(Base64.getDecoder().decode(jwt.split("\\.")[1])));
            JSONObject items = object.getJSONObject("items");
            pane.addTab(
                    InventoryType.CHAMPION.name(),
                    createStorePage(
                            client,
                            InventoryType.CHAMPION.name(),
                            items
                    )
            );
            //TODO get better at handling lots of images - I'm too stupid ~Lett4s
            //no you are not, I fixed this for you :) ~hawolt
            //kneel and bow down to King hawolt ~Lett4s
            pane.addTab(
                    InventoryType.CHAMPION_SKIN.name(),
                    createStorePage(
                            client,
                            InventoryType.CHAMPION_SKIN.name(),
                            items
                    )
            );
        } catch (Exception e) {
            Logger.error(e);
        }
        add(pane, BorderLayout.CENTER);

        LeagueClientUI.service.execute(this);
    }

    public StorePage getTabByName(String name) {
        for (int i = 0; i < pane.getTabCount(); i++) {
            if (pane.getTitleAt(i).equals(name)) {
                return (StorePage) pane.getComponentAt(i);
            }
        }
        return null;
    }

    public StorePage createStorePage(LeagueClient client, String type, JSONObject items) {
        JSONArray itemTypeArray = items.getJSONArray(type);
        List<Long> itemTypeList = itemTypeArray.toList()
                .stream()
                .map(Object::toString)
                .map(Long::parseLong)
                .toList();
        return new StorePage(
                client,
                type,
                itemTypeList,
                StoreSortProperty.values()
        );
    }

    @Override
    public void run() {
        StoreLedge store = client.getLedge().getStore();
        try {
            List<StoreItem> list = store.catalogV1();
            Map<InventoryType, StorePage> map = new HashMap<>();
            Map<InventoryType, List<StoreItem>> matches = new HashMap<>();
            for (StoreItem item : list) {
                InventoryType type = item.getInventoryType();
                if (!map.containsKey(type)) map.put(type, getTabByName(type.name()));
                StorePage page = map.get(type);
                if (page == null) continue;
                if (!cache.containsKey(type)) cache.put(type, new ArrayList<>());
                cache.get(type).add(item);
                if (!matches.containsKey(type)) matches.put(type, new ArrayList<>());
                matches.get(type).add(item);
            }
            for (InventoryType type : matches.keySet()) {
                StorePage page = map.get(type);
                if (page == null) continue;
                page.append(matches.get(type));
            }
            revalidate();
            repaint();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
