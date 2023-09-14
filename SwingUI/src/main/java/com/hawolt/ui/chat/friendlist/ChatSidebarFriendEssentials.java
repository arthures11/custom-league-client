package com.hawolt.ui.chat.friendlist;

import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LHintTextField;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;

import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Created: 09/08/2023 01:59
 * Author: Twitter @hawolt
 **/

public class ChatSidebarFriendEssentials extends ChildUIComponent implements DocumentListener {
    private final IFriendListComponent component;
    private final LHintTextField input;

    public ChatSidebarFriendEssentials(VirtualRiotXMPPClient xmppClient, IFriendListComponent component) {
        super(new BorderLayout(5, 0));
        this.setBorder(new EmptyBorder(5, 0, 0, 0));
        this.setBackground(ColorPalette.accentColor);
        this.component = component;
        input = new LHintTextField("Name");
        input.setBackground(ColorPalette.accentColor);
        add(input, BorderLayout.CENTER);
        input.getDocument().addDocumentListener(this);
        LFlatButton add = new LFlatButton("ADD", LTextAlign.CENTER, HighlightType.COMPONENT);
        add.setRounding(ColorPalette.BUTTON_SMALL_ROUNDING);
        add.setFocusPainted(false);
        add.addActionListener(listener -> {
            String name = input.getText();
            if (name.contains("#")) {
                String[] data = name.split("#");
                xmppClient.addFriendByTag(data[0], data[1]);
            } else {
                xmppClient.addFriendByName(name);
            }
            input.setText("");
        });
        add.setPreferredSize(new Dimension(50, 0));
        add(add, BorderLayout.EAST);
    }

    private void handle() {
        component.search(input.getText());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        handle();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        handle();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        handle();
    }
}
