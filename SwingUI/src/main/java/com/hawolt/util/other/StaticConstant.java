package com.hawolt.util.other;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created: 16/08/2023 18:34
 * Author: Twitter @hawolt
 **/

public class StaticConstant {

    public static final String PROJECT = "Swift-Rift";
    public static final Path APPLICATION_CACHE = Paths.get(System.getProperty("java.io.tmpdir")).resolve("swift-rift");
    public static final Path APPLICATION_SETTINGS = Paths.get(System.getProperty("user.home")).resolve(PROJECT);
    public static final String CLIENT_SETTING_fILE = ".swift-rift-client";
    public static final String PLAYER_SETTING_fILE = ".swift-rift-user";
    public static final String USER_AGENT = "Swift-Rift";
    public static final long DISCORD_APPLICATION_ID = 1147927098497957940L;

}
