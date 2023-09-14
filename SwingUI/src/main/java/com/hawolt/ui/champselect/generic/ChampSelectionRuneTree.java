package com.hawolt.ui.champselect.generic;

import com.hawolt.client.resources.communitydragon.rune.BasicRune;
import com.hawolt.client.resources.communitydragon.rune.RuneType;
import com.hawolt.ui.champselect.IRuneSelection;
import com.hawolt.ui.champselect.IncompleteRunePageException;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created: 03/09/2023 01:51
 * Author: Twitter @hawolt
 **/

public class ChampSelectionRuneTree extends ChildUIComponent implements IRuneSelection {
    private final ChampSelectionRuneRow[] components;
    private final boolean secondary, special;
    private final RuneType type;

    private int previous = -1, last = -1;

    public ChampSelectionRuneTree(RuneType type, boolean secondary) {
        this(type, secondary, true);
    }

    public ChampSelectionRuneTree(RuneType type, boolean secondary, boolean skip) {
        ColorPalette.addThemeListener(this);
        this.setLayout(new GridLayout(0, 1, 0, 5));
        this.setBackground(ColorPalette.backgroundColor);
        this.special = secondary && skip;
        this.secondary = secondary;
        this.type = type;
        LinkedList<LinkedList<BasicRune>> alignment = type.getAlignment();
        this.components = new ChampSelectionRuneRow[alignment.size()];
        for (int i = 0; i < alignment.size(); i++) {
            if (i == 0 && special) continue;
            Dimension dimension = (i == 0 && skip) ? new Dimension(64, 64) : skip ? new Dimension(48, 48) : new Dimension(32, 32);
            this.components[i] = new ChampSelectionRuneRow(this, i, alignment.get(i), dimension);
            this.add(components[i]);
        }
    }


    public BasicRune getType() {
        return type;
    }

    @Override
    public void onSelection(int componentIndex, boolean selection) {
        if (!secondary || !selection) return;
        if (previous == componentIndex) return;
        int resetTarget = last;
        this.last = previous;
        this.previous = componentIndex;
        if (resetTarget != -1 && resetTarget != componentIndex) {
            components[resetTarget].reset(-1);
        }
    }

    public BasicRune[] getSelectedRunes() throws IncompleteRunePageException {
        BasicRune[] runes = new BasicRune[secondary ? 2 : components.length];
        int helperIndex = 0;
        for (int i = 0; i < components.length; i++) {
            ChampSelectionRuneRow row = components[i];
            if (row == null || row.getLastSelection() == -1) continue;
            ChampSelectionRunePanel rune = row.getPanelComponents()[row.getLastSelection()];
            runes[secondary ? helperIndex++ : i] = rune.getRune();
        }
        for (int i = 0; i < runes.length; i++) {
            if (runes[i] == null) {
                throw new IncompleteRunePageException("UNDEFINED_RUNE_AT_INDEX" + i + ":" + secondary);
            }
        }
        return runes;
    }
}