package com.hawolt.ui.generic.themes;

import com.hawolt.ui.generic.themes.impl.*;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ColorPalette {
    public static final int CARD_ROUNDING = 25;
    public static final int BUTTON_LARGE_ROUNDING = 50;
    public static final int BUTTON_SMALL_ROUNDING = 20;
    public static Color backgroundColor = new Color(47, 54, 64);
    public static Color accentColor = new Color(38, 44, 52);
    public static Color buttonSelectionColor = new Color(64, 115, 158);
    public static Color buttonSelectionAltColor = new Color(64, 115, 158).brighter();
    public static Color textColor = new Color(255, 255, 255);
    public static Color secondaryTextColor = new Color(255, 255, 255);
    public static Color cardColor = new Color(38, 44, 52);
    public static Color dropdownColor = new Color(38, 44, 52);
    public static Color friendDND = new Color(194, 54, 22);
    public static Color friendOffline = new Color(113, 128, 147);
    public static Color friendMobile = new Color(155, 204, 155);
    public static Color friendOnline = new Color(68, 189, 50);
    public static Color friendInGame = new Color(0, 151, 230);
    public static Color friendInOtherGame = new Color(225, 177, 44);
    public static Color messageNotification = new Color(225, 177, 44);
    public static Color scrollHandleColor = new Color(150, 150, 150);
    public static Color inputUnderline = Color.DARK_GRAY;
    public static Color popupWindowColor = new Color(47, 54, 64).brighter();
    public static Color messageOut = new Color(120, 90, 40);
    public static Color messageIn = new Color(30, 35, 40);
    public static boolean useRoundedCorners = true;
    private static LThemeChoice currentThemeChoice = LThemeChoice.DEEPSEA;
    //Change listener
    private static PropertyChangeSupport themeProperty = new PropertyChangeSupport(currentThemeChoice);

    public static void setTheme(LThemeChoice theme) {
        Theme newTheme = null;

        switch (theme) {
            case DEEPSEA -> newTheme = new DeepSeaTheme();
            case RAIN -> newTheme = new RainTheme();
            case DARK -> newTheme = new DarkTheme();
            case TWILIGHT -> newTheme = new TwilightTheme();
        }

        backgroundColor = newTheme.backgroundColor;
        accentColor = newTheme.accentColor;
        buttonSelectionColor = newTheme.buttonSelectionColor;
        buttonSelectionAltColor = newTheme.buttonSelectionAltColor;
        textColor = newTheme.textColor;
        secondaryTextColor = newTheme.secondaryTextColor;
        cardColor = newTheme.cardColor;
        dropdownColor = newTheme.dropdownColor;
        friendDND = newTheme.friendDND;
        friendOffline = newTheme.friendOffline;
        friendMobile = newTheme.friendMobile;
        friendOnline = newTheme.friendOnline;
        friendInGame = newTheme.friendInGame;
        friendInOtherGame = newTheme.friendInOtherGame;
        messageNotification = newTheme.messageNotification;
        scrollHandleColor = newTheme.scrollHandleColor;
        inputUnderline = newTheme.inputUnderline;
        popupWindowColor = newTheme.popupWindowColor;
        messageOut = newTheme.messageOut;
        messageIn = newTheme.messageIn;
        fireThemeEvent(theme);
    }

    public static void addThemeListener(PropertyChangeListener listener) {
        themeProperty.addPropertyChangeListener(listener);
    }

    public static void removeThemeListener(PropertyChangeListener listener) {
        themeProperty.removePropertyChangeListener(listener);
    }

    private static void fireThemeEvent(LThemeChoice theme) {
        LThemeChoice old = currentThemeChoice;
        currentThemeChoice = theme;
        themeProperty.firePropertyChange("currentThemeChoice", old, currentThemeChoice);
    }

    public static Color getNewColor(Color color, LThemeChoice old) {
        Theme oldTheme = null;
        Theme newTheme = null;

        //Set new theme choice
        switch (old) {
            case DEEPSEA -> oldTheme = new DeepSeaTheme();
            case RAIN -> oldTheme = new RainTheme();
            case DARK -> oldTheme = new DarkTheme();
            case TWILIGHT -> oldTheme = new TwilightTheme();
        }

        //Set new theme choice
        switch (currentThemeChoice) {
            case DEEPSEA -> newTheme = new DeepSeaTheme();
            case RAIN -> newTheme = new RainTheme();
            case DARK -> newTheme = new DarkTheme();
            case TWILIGHT -> newTheme = new TwilightTheme();
        }

        if (color.equals(oldTheme.backgroundColor))
            return newTheme.backgroundColor;
        else if (color.equals(oldTheme.accentColor))
            return newTheme.accentColor;
        else if (color.equals(oldTheme.buttonSelectionColor))
            return newTheme.buttonSelectionColor;
        else if (color.equals(oldTheme.buttonSelectionAltColor))
            return newTheme.buttonSelectionAltColor;
        else if (color.equals(oldTheme.textColor))
            return newTheme.textColor;
        else if (color.equals(oldTheme.secondaryTextColor))
            return newTheme.secondaryTextColor;
        else if (color.equals(oldTheme.cardColor))
            return newTheme.cardColor;
        else if (color.equals(oldTheme.dropdownColor))
            return newTheme.dropdownColor;
        else if (color.equals(oldTheme.friendDND))
            return newTheme.friendDND;
        else if (color.equals(oldTheme.friendOffline))
            return newTheme.friendOffline;
        else if (color.equals(oldTheme.friendMobile))
            return newTheme.friendMobile;
        else if (color.equals(oldTheme.friendOnline))
            return newTheme.friendOnline;
        else if (color.equals(oldTheme.friendInGame))
            return newTheme.friendInGame;
        else if (color.equals(oldTheme.friendInOtherGame))
            return newTheme.friendInOtherGame;
        else if (color.equals(oldTheme.messageNotification))
            return newTheme.messageNotification;
        else if (color.equals(oldTheme.scrollHandleColor))
            return newTheme.scrollHandleColor;
        else if (color.equals(oldTheme.inputUnderline))
            return newTheme.inputUnderline;
        else if (color.equals(oldTheme.popupWindowColor))
            return newTheme.popupWindowColor;
        else if (color.equals(oldTheme.messageOut))
            return newTheme.messageOut;
        else if (color.equals(oldTheme.messageIn))
            return newTheme.messageIn;

        return color;
    }
}
