package com.hawolt.util.settings;

/**
 * Created: 12/09/2023 16:16
 * Author: Twitter @hawolt
 **/

public interface SettingListener<T> {
    void onSettingWrite(String name, T value);
}
