package com.hawolt.ui.generic.utility;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created: 06/08/2023 13:20
 * Author: Twitter @hawolt
 **/

public class MainUIComponent extends JPanel implements PropertyChangeListener {

    private final JFrame frame;

    protected Container container;

    public MainUIComponent(JFrame frame) {
        ColorPalette.addThemeListener(this);
        this.frame = frame;
        this.container = frame.getContentPane();
        this.container.removeAll();
        setBackground(ColorPalette.backgroundColor);
    }

    public void init() {
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public JFrame getJFrame() {
        return frame;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LThemeChoice old = (LThemeChoice) evt.getOldValue();
        setBackground(ColorPalette.getNewColor(getBackground(), old));
        repaint();
    }
}
