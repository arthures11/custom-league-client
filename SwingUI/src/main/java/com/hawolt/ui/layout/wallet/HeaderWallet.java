package com.hawolt.ui.layout.wallet;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.inventory.InventoryServiceLedge;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.subject.service.IServiceMessageListener;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.rms.data.subject.service.RiotMessageServiceMessage;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.json.JSONObject;

import java.awt.*;

/**
 * Created: 09/08/2023 16:00
 * Author: Twitter @hawolt
 **/

public class HeaderWallet extends ChildUIComponent implements Runnable, IServiceMessageListener<RiotMessageServiceMessage> {
    private final static String BASE = "https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images";
    private final LeagueClient LeagueClient;
    private final HeaderWalletCurrency be, rp;

    public HeaderWallet(LeagueClient LeagueClient) {
        super(new GridLayout(0, 1, 0, 5));
        ColorPalette.removeThemeListener(this);
        this.setPreferredSize(new Dimension(120, 0));
        this.add(be = new HeaderWalletCurrency(String.join("/", BASE, "icon-be-150.png")));
        this.add(rp = new HeaderWalletCurrency(String.join("/", BASE, "icon-rp-72.png")));
        this.LeagueClient = LeagueClient;
        this.setBackground(new Color(0, 0, 0, 0));
        LeagueClientUI.service.execute(this);
    }

    public HeaderWalletCurrency getBlueEssence() {
        return be;
    }

    public HeaderWalletCurrency getRiotPoint() {
        return rp;
    }

    @Override
    public void onMessage(RiotMessageServiceMessage riotMessageServiceMessage) {
        /*
        RiotMessageServiceMessage{
            payload=GenericRiotMessagePayload{
                payload={
                    "resource":"store/v1/wallet",
                    "payload":"{\"accountId\":2660041413928448,\"balances\":[{\"currency\":\"IP\",\"amount\":21350},{\"currency\":\"RP\",\"amount\":0}]}",
                    "service":"store-purchase",
                    "timestamp":1691592219744
                }
            }
        }

        {
           "accountId":2660041413928448,
           "balances":[
              {
                 "currency":"IP",
                 "amount":21350
              },
              {
                 "currency":"RP",
                 "amount":0
              }
           ]
        }

         */
        Logger.error(riotMessageServiceMessage);
        JSONObject balance = riotMessageServiceMessage.getPayload().getPayload();
        for (String key : balance.keySet()) {
            JSONObject detail = balance.getJSONObject(key);
            int amount = detail.getInt("amount");
            switch (key) {
                case "lol_blue_essence" -> be.deduct(amount);
                case "rp" -> rp.deduct(amount);
            }
        }
        revalidate();
    }

    @Override
    public void run() {
        LeagueClient.getRMSClient()
                .getHandler()
                .addMessageServiceListener(MessageService.CAP_WALLETS, this);
        try {
            InventoryServiceLedge ledge = LeagueClient.getLedge().getInventoryService();
            JSONObject balance = ledge.getBalances();
            Logger.error(balance);
            if (!balance.has("data")) return;
            JSONObject data = balance.getJSONObject("data");
            JSONObject balances = data.getJSONObject("balances");
            if (balances.has("lol_blue_essence")) be.setAmount(balances.getInt("lol_blue_essence"));
            if (balances.has("RP")) rp.setAmount(balances.getInt("RP"));
            revalidate();
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
