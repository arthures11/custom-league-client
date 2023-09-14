package com.hawolt.async.shutdown;

import com.hawolt.async.shutdown.hooks.*;
import com.hawolt.client.LeagueClient;
import com.hawolt.generic.runnable.ExceptionalRunnable;
import com.hawolt.generic.runnable.IExceptionCallback;
import com.hawolt.logger.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created: 28/08/2023 19:29
 * Author: Twitter @hawolt
 **/

public class ShutdownManager implements Runnable, IExceptionCallback {
    private final List<ExceptionalRunnable> list = new LinkedList<>();

    public ShutdownManager(LeagueClient client) {
        list.add(new ShutdownLeaveParty(client));
        list.add(new ShutdownPartyRegistration(client));
        list.add(new ShutdownRTMP(client));
        list.add(new ShutdownXMPP(client));
        list.add(new ShutdownRMS(client));
        list.add(new ShutdownServices(client));
        register();
    }

    public void register() {
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    @Override
    public void run() {
        for (int i = 0; i < list.size(); i++) {
            ExceptionalRunnable runnable = list.get(i);
            Logger.info("Executing ShutdownTask {}", i);
            runnable.run();
        }
    }

    @Override
    public void onException(Exception e) {
        Logger.error(e);
    }
}
