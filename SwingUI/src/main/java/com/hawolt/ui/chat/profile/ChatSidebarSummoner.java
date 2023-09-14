package com.hawolt.ui.chat.profile;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 08/08/2023 17:42
 * Author: Twitter @hawolt
 **/

public class ChatSidebarSummoner extends ChildUIComponent {
    private final ChatSidebarStatus status;
    private final ChatSideBarUIControl control;
    private final ChatSidebarName name;

    public ChatSidebarSummoner(LayoutManager layout) {
        super(layout);
        this.setBorder(new EmptyBorder(0, 5, 0, 0));
        this.setBackground(ColorPalette.accentColor);
        add(control = new ChatSideBarUIControl());
        add(name = new ChatSidebarName());
        add(status = new ChatSidebarStatus());
        status.setBorder(new EmptyBorder(0, 0, 0, 5));
    }

    public ChatSideBarUIControl getUIControl() {
        return control;
    }

    public ChatSidebarName getChatSidebarName() {
        return name;
    }

    public ChatSidebarStatus getStatus() {
        return status;
    }
}
