package com.hawolt.ui.generic.themes.impl;

import com.hawolt.ui.generic.themes.Theme;

import java.awt.*;

public class TwilightTheme extends Theme {

    //Palette reference: https://colorffy.com/dark-theme-generator
    //Primary 100 and dark 100 are the primary and dark colors used
    //Predefined primary colors
    private final Color PRIMARY_100 = new Color(56, 43, 240);
    private final Color PRIMARY_200 = new Color(94, 67, 243);
    private final Color PRIMARY_300 = new Color(122, 90, 245);
    private final Color PRIMARY_400 = new Color(145, 113, 248);
    private final Color PRIMARY_500 = new Color(166, 136, 250);
    private final Color PRIMARY_600 = new Color(186, 159, 251);
    //Predefined dark colors
    private final Color DARK_100 = new Color(18, 18, 18); //For body background color
    private final Color DARK_200 = new Color(40, 40, 40); //For cards background color
    private final Color DARK_300 = new Color(63, 63, 63); //For chips buttons, dropdowns background color
    private final Color DARK_400 = new Color(87, 87, 87); //For sidebars, navbar background color
    private final Color DARK_500 = new Color(113, 113, 113); //For modal, dialogs background color
    private final Color DARK_600 = new Color(139, 139, 139); //For on background texts color
    //Predefined mixed colors
    private final Color MIXED_100 = new Color(26, 22, 37); //For body background color
    private final Color MIXED_200 = new Color(47, 43, 58); //For cards background color
    private final Color MIXED_300 = new Color(70, 66, 79); //For chips buttons, dropdowns background color
    private final Color MIXED_400 = new Color(94, 90, 102); //For sidebars, navbar background color
    private final Color MIXED_500 = new Color(118, 115, 126); //For modal, dialogs background color
    private final Color MIXED_600 = new Color(144, 141, 150); //For on background texts color

    public TwilightTheme() {
        backgroundColor = MIXED_100;
        accentColor = MIXED_400;
        buttonSelectionColor = PRIMARY_500;
        buttonSelectionAltColor = PRIMARY_600;
        textColor = new Color(230, 230, 230);
        secondaryTextColor = MIXED_600;
        cardColor = MIXED_200;
        dropdownColor = MIXED_300;
        friendDND = new Color(194, 54, 22);
        friendOffline = new Color(113, 128, 147);
        friendMobile = new Color(155, 204, 155);
        friendOnline = new Color(68, 189, 50);
        friendInGame = new Color(0, 151, 230);
        friendInOtherGame = new Color(225, 177, 44);
        messageNotification = new Color(225, 177, 44);
        scrollHandleColor = PRIMARY_500;
        inputUnderline = MIXED_300;
        popupWindowColor = MIXED_500;
        messageOut = new Color(120, 90, 40);
        messageIn = new Color(30, 35, 40);
    }

}