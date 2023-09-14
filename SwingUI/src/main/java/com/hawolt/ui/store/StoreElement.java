package com.hawolt.ui.store;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.cache.CacheType;
import com.hawolt.client.cache.CachedValueLoader;
import com.hawolt.client.resources.ledge.store.objects.StoreItem;
import com.hawolt.client.resources.purchasewidget.CurrencyType;
import com.hawolt.client.resources.purchasewidget.PurchaseWidget;
import com.hawolt.ui.generic.component.LLabel;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.audio.AudioEngine;
import com.hawolt.util.audio.Sound;
import org.json.JSONObject;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 09/08/2023 19:01
 * Author: Twitter @hawolt
 **/

public class StoreElement extends ChildUIComponent implements IStoreElement {
    private final List<StoreButton> buttons = new ArrayList<>();
    private final LeagueClient client;
    //private final JPanel imageContainer;
    private final StoreImage image;
    private final StoreItem item;
    private final IStorePage page;

    public StoreElement(LeagueClient client, IStorePage page, StoreItem item) {
        super(new BorderLayout());
        this.setBorder(new EmptyBorder(15, 15, 15, 15));
        this.add(image = new StoreImage(item), BorderLayout.CENTER);
        this.setBackground(ColorPalette.cardColor);
        this.setPreferredSize(new Dimension(150, 300));
        this.client = client;
        this.item = item;
        this.page = page;
        this.build();
    }

    private void build() {
        if (item.isBlueEssencePurchaseAvailable() && item.getCorrectBlueEssenceCost() > 0) {
            StoreButton button = new StoreButton(this, CurrencyType.IP, item.getCorrectBlueEssenceCost());
            button.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
            button.setBackground(ColorPalette.buttonSelectionColor);
            button.setHighlightColor(ColorPalette.buttonSelectionAltColor);
            buttons.add(button);
        }
        if (item.isRiotPointPurchaseAvailable() && item.getCorrectRiotPointCost() > 0) {
            StoreButton button = new StoreButton(this, CurrencyType.RP, item.getCorrectRiotPointCost());
            button.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
            button.setBackground(ColorPalette.buttonSelectionColor);
            button.setHighlightColor(ColorPalette.buttonSelectionAltColor);
            buttons.add(button);
        }
        ChildUIComponent mainComponent = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        ChildUIComponent nameComponent = new ChildUIComponent(new GridLayout(0, 1, 0, 0));
        LLabel name = new LLabel(this.item.getName(), LTextAlign.CENTER);
        name.setBackground(ColorPalette.cardColor);
        nameComponent.add(name);
        mainComponent.add(nameComponent);
        mainComponent.setBackground(ColorPalette.cardColor);
        ChildUIComponent buttonComponent = new ChildUIComponent(new GridLayout(0, buttons.isEmpty() ? 1 : buttons.size(), 5, 0));
        for (StoreButton button : buttons) {
            buttonComponent.add(button);
        }
        buttonComponent.setBackground(ColorPalette.cardColor);
        mainComponent.add(buttonComponent);
        add(mainComponent, BorderLayout.SOUTH);
        revalidate();
    }

    public StoreItem getItem() {
        return item;
    }

    public StoreImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(),
                ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0, ColorPalette.useRoundedCorners ? ColorPalette.CARD_ROUNDING : 0);

    }

    @Override
    public void purchase(CurrencyType currencyType, long price) {
        LeagueClientUI.service.execute(() -> {
            try {
                PurchaseWidget widget = client.getPurchaseWidget();
                JSONObject response = new JSONObject(widget.purchase(currencyType, item.getInventoryType(), item.getItemId(), price));
                if (response.has("errorCode")) {
                    AudioEngine.play(Sound.ERROR);
                } else {
                    AudioEngine.play(Sound.SUCCESS);
                    page.removeStoreElement(this);
                    LeagueClientUI.service.execute(
                            new CachedValueLoader<>(
                                    CacheType.INVENTORY_TOKEN,
                                    () -> client.getLedge().getInventoryService().getInventoryToken(),
                                    client
                            )
                    );
                    revalidate();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}