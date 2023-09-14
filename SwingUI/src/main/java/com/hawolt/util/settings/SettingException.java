package com.hawolt.util.settings;

/**
 * Created: 28/08/2023 19:17
 * Author: Twitter @hawolt
 **/

public class SettingException extends Exception {
    public SettingException(String type) {
        super("Setting for " + type + " does not exist");
    }
}
