package com.hawolt.ui.champselect.postgame;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 11/09/2023 21:34
 * Author: Twitter @hawolt
 **/

public class PostGameScoreboard extends ChildUIComponent {

    public PostGameScoreboard() {
        super(new GridLayout(0, 1, 0, 20));
    }

    private ChildUIComponent createScoreboard(JSONArray array) {
        ChildUIComponent component = new ChildUIComponent(new GridLayout(Math.max(1, array.length()), 1, 0, 5));
        component.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        component.setBorder(new EmptyBorder(5, 5, 5, 5));
        for (int i = 0; i < array.length(); i++) {
            JSONObject participant = array.getJSONObject(i);
            JSONArray statistics = participant.getJSONArray("statistics");
            String description = String.format(
                    "%-16s %-2s / %-2s / %-2s",
                    participant.getString("summonerName"),
                    getValue("CHAMPIONS_KILLED", statistics),
                    getValue("NUM_DEATHS", statistics),
                    getValue("ASSISTS", statistics)
            );
            component.add(createTextLabel(description, SwingConstants.LEFT));
        }
        return component;
    }

    private long getValue(String statTypeName, JSONArray statistics) {
        for (int i = 0; i < statistics.length(); i++) {
            JSONObject stat = statistics.getJSONObject(i);
            if (!statTypeName.equals(stat.getString("statTypeName"))) continue;
            return stat.getLong("value");
        }
        return 0L;
    }

    private JLabel createTextLabel(Object o, int alignment) {
        JLabel label = new JLabel(o.toString(), alignment);
        label.setBackground(ColorPalette.backgroundColor);
        label.setForeground(ColorPalette.textColor);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        return label;
    }

    public void update(JSONObject data) {
        removeAll();
        add(createScoreboard(data.getJSONArray("teamPlayerParticipantStats")));
        add(createScoreboard(data.getJSONArray("otherTeamPlayerParticipantStats")));
        revalidate();
    }
}
