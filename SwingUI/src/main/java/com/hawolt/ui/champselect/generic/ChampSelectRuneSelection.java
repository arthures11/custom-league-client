package com.hawolt.ui.champselect.generic;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.communitydragon.rune.BasicRune;
import com.hawolt.client.resources.communitydragon.rune.RuneIndex;
import com.hawolt.client.resources.communitydragon.rune.RuneSource;
import com.hawolt.client.resources.communitydragon.rune.RuneType;
import com.hawolt.ui.champselect.IncompleteRunePageException;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTabbedPane;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created: 02/09/2023 19:19
 * Author: Twitter @hawolt
 **/

public class ChampSelectRuneSelection extends ChampSelectUIComponent {
    private final ChampSelectionRuneTree extra;
    private final LTabbedPane main, secondary;
    private final LFlatButton close, save;
    private int selected = -1;

    public ChampSelectRuneSelection(String patch) {
        this.setLayout(new BorderLayout());
        //this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        ChildUIComponent header;
        this.add(header = new ChildUIComponent(new BorderLayout()), BorderLayout.NORTH);
        header.add(close = new LFlatButton("×", LTextAlign.CENTER, HighlightType.COMPONENT), BorderLayout.EAST);
        close.setRoundingCorners(true, false, true, false);
        close.setRounding(ColorPalette.CARD_ROUNDING);
        header.add(save = new LFlatButton("Save", LTextAlign.CENTER, HighlightType.COMPONENT), BorderLayout.WEST);
        save.setRoundingCorners(false, true, false, true);
        save.setRounding(ColorPalette.CARD_ROUNDING);
        save.addActionListener(listener -> setRuneSelection());
        ChildUIComponent panel = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        this.add(panel, BorderLayout.CENTER);
        this.main = new LTabbedPane(new Font("Dialog", Font.PLAIN, 10));
        panel.add(main);
        this.secondary = new LTabbedPane(new Font("Dialog", Font.PLAIN, 10));
        ChildUIComponent right = new ChildUIComponent(new BorderLayout());
        right.add(secondary, BorderLayout.CENTER);
        panel.add(right);
        RuneIndex runeIndex = RuneSource.INSTANCE.get(patch);
        extra = new ChampSelectionRuneTree(runeIndex.getAdditional(), false, false);
        extra.setPreferredSize(new Dimension(0, 150));
        extra.setBorder(new EmptyBorder(0, 0, 10, 0));
        right.add(extra, BorderLayout.SOUTH);
        LinkedList<RuneType> list = runeIndex.getMain();
        for (RuneType type : list) {
            main.addTab(type.getName(), new ChampSelectionRuneTree(type, false));
            secondary.addTab(type.getName(), new ChampSelectionRuneTree(type, true));
        }
        disableTabAndSelectNextAvailable(secondary, 0);
        main.addChangeListener(listener -> disableTabAndSelectNextAvailable(secondary, main.getSelectedIndex()));
    }


    private void setRuneSelection() {
        LeagueClientUI.service.execute(() -> {
            try {
                JSONObject runes = getSelectedRunes();
                LeagueClient client = context.getChampSelectDataContext().getLeagueClient();
                client.getLedge().getPerks().setRunesForCurrentRegistration(runes);
                JOptionPane.showMessageDialog(Frame.getFrames()[0], "Rune Page set");
            } catch (IncompleteRunePageException e) {
                JOptionPane.showMessageDialog(Frame.getFrames()[0], "Rune Page incomplete");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(Frame.getFrames()[0], "Failed to save Rune Page");
            }
        });
    }

    public JSONObject getSelectedRunes() throws IncompleteRunePageException {
        JSONObject runes = new JSONObject();
        JSONArray perkIds = new JSONArray();
        ChampSelectionRuneTree primaryStyle = getSelectedRuneType(main);
        ChampSelectionRuneTree subStyle = getSelectedRuneType(secondary);
        for (BasicRune rune : getAllRunesOrdered(primaryStyle, subStyle, extra)) {
            perkIds.put(rune.getId());
        }
        runes.put("perkIds", perkIds);
        runes.put("perkStyle", primaryStyle.getType().getId());
        runes.put("perkSubStyle", subStyle.getType().getId());
        return runes;
    }

    private void disableTabAndSelectNextAvailable(JTabbedPane pane, int index) {
        if (selected != -1) pane.setEnabledAt(selected, true);
        pane.setEnabledAt(selected = index, false);
        if (pane.getSelectedIndex() != index) return;
        pane.setSelectedIndex((++index) % pane.getTabCount());
    }

    private ChampSelectionRuneTree getSelectedRuneType(JTabbedPane pane) {
        return ((ChampSelectionRuneTree) pane.getComponentAt(pane.getSelectedIndex()));
    }

    private BasicRune[] getAllRunesOrdered(ChampSelectionRuneTree... panels) throws IncompleteRunePageException {
        List<BasicRune> runes = new ArrayList<>();
        for (ChampSelectionRuneTree panel : panels) {
            runes.addAll(Arrays.asList(panel.getSelectedRunes()));
        }
        return runes.toArray(BasicRune[]::new);
    }

    public LFlatButton getCloseButton() {
        return close;
    }

    public LFlatButton getSaveButton() {
        return save;
    }
}
