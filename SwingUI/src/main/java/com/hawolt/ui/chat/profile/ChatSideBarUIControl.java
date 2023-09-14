package com.hawolt.ui.chat.profile;

import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * Created: 08/08/2023 17:45
 * Author: Twitter @hawolt
 **/

public class ChatSideBarUIControl extends JComponent implements ActionListener {

    private final ChildUIComponent buttons = new ChildUIComponent(new GridLayout(0, 3, 3, 0));

    private LFlatButton minimize, maximize, close;
    private boolean maximized;
    private Rectangle bounds;
    private int state;

    public ChatSideBarUIControl() {
        this.setLayout(new BorderLayout());
        this.setBackground(ColorPalette.accentColor);
        this.add(buttons, BorderLayout.EAST);
        this.setup();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame frame = (JFrame) Frame.getFrames()[0];
        switch (e.getActionCommand()) {
            case "—" -> {
                frame.setState(JFrame.ICONIFIED);
            }
            case "▭" -> {
                if (!maximized) {
                    bounds = frame.getBounds();
                    state = frame.getExtendedState();
                    frame.setExtendedState(state | JFrame.MAXIMIZED_BOTH);
                } else {
                    frame.setState(state);
                    frame.setBounds(bounds);
                }
                this.maximized = !maximized;
            }
            case "×" -> {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }
    }

    private void setup() {
        buttons.setBackground(ColorPalette.accentColor);
        minimize = new LFlatButton("—", LTextAlign.CENTER, HighlightType.COMPONENT);
        minimize.addActionListener(this);
        buttons.add(minimize);
        maximize = new LFlatButton("▭", LTextAlign.CENTER, HighlightType.COMPONENT);
        maximize.addActionListener(this);
        buttons.add(maximize);
        close = new LFlatButton("×", LTextAlign.CENTER, HighlightType.COMPONENT);
        close.addActionListener(this);
        close.setHighlightColor(Color.RED);
        buttons.add(close);
    }

    public ChildUIComponent getButtonPanel() {
        return buttons;
    }

    public LFlatButton getMinimize() {
        return minimize;
    }

    public LFlatButton getMaximize() {
        return maximize;
    }

    public LFlatButton getClose() {
        return close;
    }
}
