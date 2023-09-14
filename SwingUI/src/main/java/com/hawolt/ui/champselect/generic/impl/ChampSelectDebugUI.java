package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.ui.champselect.context.ChampSelectSettingsContext;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.generic.themes.ColorPalette;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Created: 29/08/2023 17:11
 * Author: Twitter @hawolt
 **/

public class ChampSelectDebugUI extends ChampSelectUIComponent {
    private final JLabel queueId, counter, phaseName;

    public ChampSelectDebugUI() {
        ColorPalette.addThemeListener(this);
        this.setBackground(ColorPalette.backgroundColor);
        this.setLayout(new GridLayout(0, 3, 5, 0));
        this.setBorder(
                new CompoundBorder(
                        new MatteBorder(0, 1, 1, 1, Color.DARK_GRAY),
                        new EmptyBorder(2, 5, 2, 5)
                )
        );
        this.add(queueId = new JLabel("", SwingConstants.LEFT));
        this.queueId.setForeground(ColorPalette.textColor);
        this.add(phaseName = new JLabel("", SwingConstants.CENTER));
        this.phaseName.setForeground(ColorPalette.textColor);
        this.add(counter = new JLabel("", SwingConstants.RIGHT));
        this.counter.setForeground(ColorPalette.textColor);
    }

    @Override
    public void update() {
        ChampSelectSettingsContext settingsContext = context.getChampSelectSettingsContext();
        this.queueId.setText(String.format("Queue ID: %s", settingsContext.getQueueId()));
        this.counter.setText(String.format("COUNTER: %s", settingsContext.getCounter()));
        this.phaseName.setText(String.format("PHASE: %s - %s", settingsContext.getPhaseName(), settingsContext.getSubphase()));
    }

}
