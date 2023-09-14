package com.hawolt.ui.champselect.postgame;

import com.hawolt.client.resources.ledge.leagues.objects.LeagueNotification;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created: 11/09/2023 21:30
 * Author: Twitter @hawolt
 **/

public class PostGameHeader extends ChildUIComponent {

    public PostGameHeader() {
        super(new BorderLayout());
        this.setPreferredSize(new Dimension(0, 100));
    }

    private String convertMStoTimestamp(long elapsed) {
        long hours = TimeUnit.MILLISECONDS.toHours(elapsed);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsed));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed));
        String secondsAsText = String.format("%02d", seconds);
        String minutesAsText = hours > 0 ? String.format("%02d", minutes) : String.valueOf(minutes);
        StringBuilder duration = new StringBuilder();
        if (hours > 0) duration.append(hours).append(":");
        duration.append(minutesAsText).append(":");
        duration.append(secondsAsText);
        return duration.toString();
    }

    private void addNotification(LeagueNotification leagueNotification) {
        ChildUIComponent additional = new ChildUIComponent(new GridLayout(0, 1, 0, 5));
        String rank = String.format("%s %s %s LP", leagueNotification.getTier(), leagueNotification.getRank(), leagueNotification.getLeaguePoints());
        additional.add(createTextLabel(String.format("%s LP", leagueNotification.getLeaguePointsDelta()), SwingConstants.RIGHT));
        additional.add(createTextLabel(rank, SwingConstants.RIGHT));
        add(additional, BorderLayout.EAST);
        revalidate();
    }

    private JLabel createTextLabel(Object o, int alignment) {
        JLabel label = new JLabel(o.toString(), alignment);
        label.setBackground(ColorPalette.backgroundColor);
        label.setForeground(ColorPalette.textColor);
        label.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        return label;
    }

    public void update(JSONObject data, List<LeagueNotification> notifications) {
        removeAll();
        int gameLengthInSeconds = data.getInt("gameLength");
        String queueType = data.getString("queueType");
        String gameMode = data.getString("gameMode");
        long gameId = data.getLong("gameId");
        ChildUIComponent basic = new ChildUIComponent(new GridLayout(0, 4, 5, 0));
        basic.add(createTextLabel(gameMode, SwingConstants.LEFT));
        basic.add(createTextLabel(queueType, SwingConstants.LEFT));
        basic.add(createTextLabel(convertMStoTimestamp(TimeUnit.SECONDS.toMillis(gameLengthInSeconds)), SwingConstants.LEFT));
        basic.add(createTextLabel(String.valueOf(gameId), SwingConstants.LEFT));
        add(basic, BorderLayout.CENTER);
        notifications.stream()
                .filter(o -> o.getGameId() == gameId)
                .findFirst()
                .ifPresent(this::addNotification);
        revalidate();
    }
}
