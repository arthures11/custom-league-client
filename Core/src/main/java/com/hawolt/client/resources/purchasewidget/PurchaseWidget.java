package com.hawolt.client.resources.purchasewidget;

import com.hawolt.authentication.WebOrigin;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.UndocumentedEndpoint;
import com.hawolt.client.resources.ledge.store.objects.InventoryType;
import com.hawolt.generic.Constant;
import com.hawolt.http.OkHttp3Client;
import com.hawolt.virtual.leagueclient.authentication.OAuthToken;
import com.hawolt.virtual.leagueclient.instance.IVirtualLeagueClientInstance;
import com.hawolt.yaml.ConfigValue;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: 09/08/2023 21:48
 * Author: Twitter @hawolt
 **/

public class PurchaseWidget extends UndocumentedEndpoint {
    public PurchaseWidget(LeagueClient client) {
        super(client, client.getVirtualLeagueClient().getYamlWrapper().get(ConfigValue.LEDGE));
    }

    public String purchase(CurrencyType currency, InventoryType type, long itemId, long price) throws IOException {
        String uri = String.format("%s/%s/v%s/purchase",
                base,
                name(),
                version()
        );
        IVirtualLeagueClientInstance instance = client.getVirtualLeagueClient()
                .getVirtualLeagueClientInstance();
        String version = instance.getLocalLeagueFileVersion().getVersionValue(
                client.getVirtualLeagueClientInstance().getPlatform(),
                "LeagueClientUxRender.exe"
        );
        String agent = String.format("LeagueOfLegendsClient/%s (%s)", version, rcp());
        OAuthToken auth = client.getVirtualLeagueClient()
                .getWebOriginOAuthTokenMap()
                .get(WebOrigin.LOL_LOGIN);
        String bearer = auth.get("oauthtoken.access_token", true);
        JSONObject object = new JSONObject();
        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        JSONObject itemKey = new JSONObject();
        itemKey.put("inventoryType", type.name());
        itemKey.put("itemId", itemId);
        JSONObject purchaseCurrencyInfo = new JSONObject();
        purchaseCurrencyInfo.put("currencyType", currency.name());
        purchaseCurrencyInfo.put("price", price);
        purchaseCurrencyInfo.put("purchasable", true);
        item.put("itemKey", itemKey);
        item.put("purchaseCurrencyInfo", purchaseCurrencyInfo);
        item.put("quantity", 1);
        item.put("source", "cdp");
        items.put(item);
        object.put("items", items);
        Request request = new Request.Builder()
                .url(uri)
                .addHeader("Authorization", String.format("Bearer %s", bearer))
                .addHeader("User-Agent", agent)
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(object.toString(), Constant.APPLICATION_JSON))
                .build();
        Call call = OkHttp3Client.perform(request, gateway);
        try (Response response = call.execute()) {
            try (ResponseBody body = response.body()) {
                return body.string();
            }
        }
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String name() {
        return "purchasewidget";
    }

    @Override
    public String rcp() {
        return "rcp-be-lol-purchase-widget";
    }
}
