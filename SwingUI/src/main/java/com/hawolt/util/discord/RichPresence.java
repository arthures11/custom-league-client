package com.hawolt.util.discord;

import com.hawolt.async.ExecutorManager;
import com.hawolt.logger.Logger;
import com.hawolt.util.other.StaticConstant;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.LogLevel;
import de.jcm.discordgamesdk.activity.Activity;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.ExecutorService;

/**
 * Created: 03/09/2023 18:10
 * Author: Twitter @hawolt
 **/

public class RichPresence {

    public static void show() {
        ExecutorService loader = ExecutorManager.getService("rpc-loader");
        loader.execute(() -> {
            try {
                File discordLibrary = DiscordLibraryManager.downloadDiscordLibrary();
                if (discordLibrary == null) throw new Exception("Failed to download library");
                Core.init(discordLibrary);
                try (CreateParams params = new CreateParams()) {
                    params.setClientID(StaticConstant.DISCORD_APPLICATION_ID);
                    params.setFlags(CreateParams.getDefaultFlags());
                    try (Core core = new Core(params)) {
                        core.setLogHook(LogLevel.ERROR, (logLevel, s) -> {
                            Logger.error("[discord-rpc] if rpc is working this can be ignored: {}", s);
                        });
                        try (Activity activity = new Activity()) {
                            activity.setDetails("custom-league-client");
                            activity.setState("Github @hawolt");
                            activity.timestamps().setStart(Instant.now());
                            activity.assets().setLargeImage("fullsize-logo");
                            core.activityManager().updateActivity(activity);
                        }
                        while (true) {
                            core.runCallbacks();
                            try {
                                Thread.sleep(16);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("[discord-rpc] an error has occurred");
                Logger.error(e);
            }
        });
        loader.shutdown();
    }
}
