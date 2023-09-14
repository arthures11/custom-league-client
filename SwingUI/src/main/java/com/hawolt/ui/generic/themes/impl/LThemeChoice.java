package com.hawolt.ui.generic.themes.impl;

public enum LThemeChoice {
    DEEPSEA("Deep Sea"),
    RAIN("Rain"),
    DARK("Dark"),
    TWILIGHT("Twilight");

    private final String theme;

    LThemeChoice(String theme) {
        this.theme = theme;
    }

    public String getTheme() {
        return theme;
    }

    @Override
    public String toString() {
        return super.toString().replaceAll("_", " ");
    }
}
