package com.hawolt.ui.champselect.generic;

import com.hawolt.ui.champselect.context.*;
import com.hawolt.ui.generic.utility.ChildUIComponent;

/**
 * Created: 29/08/2023 17:13
 * Author: Twitter @hawolt
 **/

public abstract class ChampSelectUIComponent extends ChildUIComponent {
    protected ChampSelectInteractionContext interactionContext;
    protected ChampSelectInterfaceContext interfaceContext;
    protected ChampSelectSettingsContext settingsContext;
    protected ChampSelectUtilityContext utilityContext;
    protected ChampSelectDataContext dataContext;
    protected ChampSelectContext context;

    public void configure(ChampSelectContext context) {
        this.interactionContext = context.getChampSelectInteractionContext();
        this.interfaceContext = context.getChampSelectInterfaceContext();
        this.settingsContext = context.getChampSelectSettingsContext();
        this.utilityContext = context.getChampSelectUtilityContext();
        this.dataContext = context.getChampSelectDataContext();
        this.context = context;
    }

    public void execute(int initialCounter) {
        if (context == null) return;
        if (context.getChampSelectSettingsContext().getCounter() == initialCounter) {
            this.init();
        }
        this.update();
    }

    public void update() {

    }

    public void init() {

    }
}
