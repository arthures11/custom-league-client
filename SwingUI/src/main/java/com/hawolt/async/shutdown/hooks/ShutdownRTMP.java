package com.hawolt.async.shutdown.hooks;

import com.hawolt.async.shutdown.ShutdownTask;
import com.hawolt.client.LeagueClient;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.utility.PacketCallback;

/**
 * Created: 16/08/2023 18:06
 * Author: Twitter @hawolt
 **/

public class ShutdownRTMP extends ShutdownTask implements PacketCallback {
    public ShutdownRTMP(LeagueClient client) {
        super(client);
    }

    @Override
    protected void execute() throws Exception {
        client.getRTMPClient().getTeamBuilderService().quitGameV2Asynchronous(this);
        client.getRTMPClient().getLoginService().logoutAsynchronous(this);
        client.getRTMPClient().disconnect();
    }

    @Override
    public void onPacket(RtmpPacket rtmpPacket, TypedObject typedObject) {
        Logger.info("Logout-Flow Response {}", typedObject);
    }
}
