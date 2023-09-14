package com.hawolt.ui.generic.themes.impl;

import com.hawolt.ui.generic.themes.Theme;

import java.awt.*;

public class DeepSeaTheme extends Theme {

    public DeepSeaTheme() {
        backgroundColor = new Color(47, 54, 64);
        accentColor = new Color(38, 44, 52);
        buttonSelectionColor = new Color(64, 115, 158);
        buttonSelectionAltColor = new Color(64, 115, 158).brighter();
        textColor = new Color(255, 255, 255);
        secondaryTextColor = new Color(255, 255, 255);
        cardColor = new Color(38, 44, 52);
        dropdownColor = new Color(38, 44, 52);
        friendDND = new Color(194, 54, 22);
        friendOffline = new Color(113, 128, 147);
        friendMobile = new Color(155, 204, 155);
        friendOnline = new Color(68, 189, 50);
        friendInGame = new Color(0, 151, 230);
        friendInOtherGame = new Color(225, 177, 44);
        messageNotification = new Color(225, 177, 44);
        scrollHandleColor = new Color(150, 150, 150);
        inputUnderline = Color.DARK_GRAY;
        popupWindowColor = new Color(47, 54, 64).brighter();
        messageOut = new Color(120, 90, 40);
        messageIn = new Color(30, 35, 40);
    }

}
