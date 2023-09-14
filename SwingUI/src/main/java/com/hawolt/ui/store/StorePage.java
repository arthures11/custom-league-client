package com.hawolt.ui.store;

import com.hawolt.async.Debouncer;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.misc.SortOrder;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.ledge.store.objects.StoreSortProperty;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LComboBox;
import com.hawolt.ui.generic.component.LHintTextField;
import com.hawolt.ui.generic.component.LScrollPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created: 09/08/2023 18:45
 * Author: Twitter @hawolt
 **/

public class StorePage extends ChildUIComponent implements IStorePage {

    private final Map<Long, StoreElement> map = new HashMap<>();
    private final LeagueClient client;
    private final ChildUIComponent grid;
    private final List<Long> owned;
    private final String name;
    private final String[] options = {"SKINS", "CHROMAS"};
    private final StoreElementComparator alphabeticalComparator = new StoreElementComparator(StoreSortProperty.NAME, SortOrder.ASCENDING);
    private final StoreElementComparator comparator;
    private final Debouncer debouncer = new Debouncer();
    private final ChildUIComponent inputPanel;
    private String filter = "";
    private boolean chromaFilter = false;

    public StorePage(LeagueClient client, String name, List<Long> owned, StoreSortProperty... properties) {
        super(new BorderLayout(0, 5));
        this.client = client;
        this.owned = owned;
        this.name = name;
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        grid = new ChildUIComponent(new GridLayout(0, 5, 15, 15));
        add(component, BorderLayout.NORTH);
        component.add(grid, BorderLayout.NORTH);
        LScrollPane scrollPane = new LScrollPane(component);
        scrollPane.setBackground(ColorPalette.backgroundColor);
        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Rectangle visible = grid.getVisibleRect();
                for (Component child : grid.getComponents()) {
                    if (child instanceof StoreElement element) {
                        Rectangle bounds = element.getBounds();
                        debouncer.debounce(String.valueOf(element.getItem().getItemId()), () -> {
                            if (bounds.intersects(visible)) {
                                element.getImage().load();
                            } else {
                                element.getImage().unload();
                            }
                        }, 100L, TimeUnit.MILLISECONDS);
                    }
                }
            }
        });
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        setBorder(new EmptyBorder(5, 5, 5, 0));

        comparator = new StoreElementComparator(properties.length > 0 ? properties[0] : null, SortOrder.DESCENDING);
        inputPanel = createInputPanel(properties);
        this.add(inputPanel, BorderLayout.NORTH);
    }

    @NotNull
    private ChildUIComponent createInputPanel(StoreSortProperty[] properties) {
        LComboBox<StoreSortOption> sortBox = createStoreSortOptionJComboBox(properties);

        ChildUIComponent inputPanel = new ChildUIComponent();
        inputPanel.setBackground(ColorPalette.backgroundColor);
        inputPanel.setLayout(new GridLayout(1, 2, 5, 0));
        inputPanel.add(sortBox);
        if (this.name.equals(InventoryType.CHAMPION_SKIN.name())) {
            LComboBox<String> options = new LComboBox<>(this.options);
            options.addItemListener(listener -> {
                chromaFilter = !Objects.equals(options.getSelectedItem(), this.options[0]);
                updateElements();
            });
            inputPanel.add(options);
        }
        LHintTextField search = new LHintTextField("Search...");

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filter = search.getText().toLowerCase();
                debouncer.debounce("searchField", () -> updateElements(), 200, TimeUnit.MILLISECONDS);
            }
        });

        inputPanel.add(search);
        return inputPanel;
    }

    @NotNull
    private LComboBox<StoreSortOption> createStoreSortOptionJComboBox(StoreSortProperty[] properties) {
        LComboBox<StoreSortOption> sortBox = new LComboBox<>();
        for (StoreSortProperty property : properties) {
            sortBox.addItem(new StoreSortOption(property, SortOrder.DESCENDING));
            sortBox.addItem(new StoreSortOption(property, SortOrder.ASCENDING));
        }
        sortBox.addItemListener(listener -> {
            StoreSortOption option = sortBox.getItemAt(sortBox.getSelectedIndex());
            comparator.setProperty(option.property());
            comparator.setOrder(option.order());
            updateElements();
        });
        return sortBox;
    }

    public ChildUIComponent getGrid() {
        return grid;
    }

    public void append(StoreItem item) {
        append(Collections.singletonList(item));
    }

    public void append(List<StoreItem> items) {
        try {
            for (StoreItem item : items) {
                if (owned.contains(item.getItemId())) continue;
                if (item.getRiotPointCost() == 0 && !item.isBlueEssencePurchaseAvailable()) continue;
                JSONObject object = item.asJSON();
                long itemId = object.getLong("itemId");
                StoreElement element = new StoreElement(client, this, item);
                map.put(itemId, element);
                grid.add(element);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        updateElements();
    }

    @Override
    public void removeStoreElement(StoreElement component) {
        grid.remove(component);
        map.remove(component.getItem().getItemId());
        updateElements();
    }

    public void updateElements() {
        grid.removeAll();
        map.values()
                .stream()
                .sorted(this.alphabeticalComparator)
                .sorted(this.comparator)
                .filter(champion -> champion.getItem().getName().toLowerCase().contains(filter))
                .filter(skin -> skin.getItem().hasSubInventoryType() == chromaFilter)
                .forEach(this.grid::add);
        revalidate();
        repaint();
    }
}
