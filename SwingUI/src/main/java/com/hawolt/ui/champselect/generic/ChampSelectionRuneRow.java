package com.hawolt.ui.champselect.generic;

import com.hawolt.client.resources.communitydragon.rune.BasicRune;
import com.hawolt.ui.champselect.IRuneSelection;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created: 03/09/2023 01:52
 * Author: Twitter @hawolt
 **/

public class ChampSelectionRuneRow extends ChildUIComponent implements IRuneSelection {
    private final ChampSelectionRunePanel[] components;
    private final IRuneSelection selection;
    private final int componentIndex;
    private int lastSelection = -1;

    public ChampSelectionRuneRow(IRuneSelection selection, int componentIndex, LinkedList<BasicRune> list, Dimension dimension) {
        ColorPalette.addThemeListener(this);
        this.setLayout(new GridLayout(0, list.size(), 5, 0));
        this.setBackground(ColorPalette.backgroundColor);
        this.selection = selection;
        this.componentIndex = componentIndex;
        this.components = new ChampSelectionRunePanel[list.size()];
        for (int i = 0; i < list.size(); i++) {
            this.components[i] = new ChampSelectionRunePanel(this, i, list.get(i), dimension);
            add(this.components[i]);
        }
    }

    public ChampSelectionRunePanel[] getPanelComponents() {
        return components;
    }

    public int getLastSelection() {
        return lastSelection;
    }

    @Override
    public void onSelection(int componentIndex, boolean selected) {
        if (!selected) {
            this.lastSelection = -1;
        } else {
            this.selection.onSelection(this.componentIndex, true);
            this.reset(componentIndex);
        }
    }

    public void reset(int exception) {
        this.lastSelection = exception;
        for (int i = 0; i < components.length; i++) {
            if (i == exception) continue;
            components[i].setSelected(false);
        }
    }
}