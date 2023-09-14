package com.hawolt.ui.profile;

import com.hawolt.LeagueClientUI;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import java.awt.*;

/**
 * Created: 12/09/2023 19:54
 * Author: Twitter @hawolt
 **/

public class ProfileWindow extends ChildUIComponent {
    private final LeagueClientUI leagueClientUI;

    public ProfileWindow(LeagueClientUI leagueClientUI) {
        super(new BorderLayout());
        this.leagueClientUI = leagueClientUI;
    }


}
