package com.hawolt.async.shutdown;

import com.hawolt.client.LeagueClient;
import com.hawolt.generic.runnable.ExceptionalRunnable;

/**
 * Created: 16/08/2023 18:11
 * Author: Twitter @hawolt
 **/

public abstract class ShutdownTask extends ExceptionalRunnable {
    protected final LeagueClient client;

    public ShutdownTask(LeagueClient client) {
        this.client = client;
    }
}
