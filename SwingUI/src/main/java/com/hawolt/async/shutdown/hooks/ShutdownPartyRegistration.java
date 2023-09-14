package com.hawolt.async.shutdown.hooks;

import com.hawolt.async.shutdown.ShutdownTask;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.PartiesRegistration;
import com.hawolt.client.resources.ledge.parties.objects.data.PartyAction;

/**
 * Created: 16/08/2023 18:06
 * Author: Twitter @hawolt
 **/

public class ShutdownPartyRegistration extends ShutdownTask {
    public ShutdownPartyRegistration(LeagueClient client) {
        super(client);
    }

    @Override
    protected void execute() throws Exception {
        PartiesLedge ledge = client.getLedge().getParties();
        PartiesRegistration registration = ledge.getCurrentRegistration();
        if (registration == null) return;
        ledge.setQueueAction(PartyAction.STOP);
    }
}
