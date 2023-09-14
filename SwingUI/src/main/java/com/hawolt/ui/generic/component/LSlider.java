package com.hawolt.ui.generic.component;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LSlider extends JSlider implements PropertyChangeListener {

    public LSlider(int orientation, int min, int max, int value) {
        super(orientation, min, max, value);
        init();
    }

    private void init() {
        ColorPalette.addThemeListener(this);
        setFont(new Font("Dialog", Font.BOLD, 14));
        setBackground(ColorPalette.backgroundColor);
        setForeground(ColorPalette.textColor);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LThemeChoice old = (LThemeChoice) evt.getOldValue();
        setBackground(ColorPalette.getNewColor(getBackground(), old));
        setForeground(ColorPalette.getNewColor(getForeground(), old));
    }
}
