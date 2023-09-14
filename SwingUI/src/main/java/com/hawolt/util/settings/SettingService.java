package com.hawolt.util.settings;

/**
 * Created: 28/08/2023 18:53
 * Author: Twitter @hawolt
 **/

public interface SettingService {
    UserSettings set(String username);

    UserSettings getUserSettings();

    ClientSettings getClientSettings();

    void write(SettingType type, String name, Object o);

    void addSettingListener(String name, SettingListener<?> listener);
}
