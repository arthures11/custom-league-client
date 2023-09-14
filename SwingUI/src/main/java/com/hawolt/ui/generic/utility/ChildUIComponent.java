package com.hawolt.ui.generic.utility;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created: 06/08/2023 13:39
 * Author: Twitter @hawolt
 **/

public class ChildUIComponent extends JPanel implements PropertyChangeListener {
    public ChildUIComponent() {
        ColorPalette.addThemeListener(this);
    }

    public ChildUIComponent(LayoutManager layout) {
        ColorPalette.addThemeListener(this);
        setBackground(ColorPalette.backgroundColor);
        setLayout(layout);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LThemeChoice old = (LThemeChoice) evt.getOldValue();
        setBackground(ColorPalette.getNewColor(getBackground(), old));
        setForeground(ColorPalette.getNewColor(getForeground(), old));
    }
}