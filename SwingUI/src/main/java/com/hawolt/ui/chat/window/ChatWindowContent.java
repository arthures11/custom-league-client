package com.hawolt.ui.chat.window;

import com.hawolt.rtmp.amf.Pair;
import com.hawolt.ui.generic.component.LHintTextField;
import com.hawolt.ui.generic.component.LScrollPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.SmartScroller;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 08/08/2023 21:09
 * Author: Twitter @hawolt
 **/

public class ChatWindowContent extends ChildUIComponent {
    private final List<Pair<ChatPerspective, String>> queue = new ArrayList<>();
    private final ChildUIComponent history = new ChildUIComponent();
    private final Object lock = new Object();
    private final LHintTextField input;
    private final LScrollPane pane;

    public ChatWindowContent(String jid, VirtualRiotXMPPClient xmppClient, LayoutManager layout) {
        super(layout);
        setBackground(ColorPalette.popupWindowColor);
        history.setLayout(new BoxLayout(history, BoxLayout.Y_AXIS));
        history.setBackground(ColorPalette.popupWindowColor);
        history.setBorder(new EmptyBorder(5, 0, 5, 0));
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        component.add(history, BorderLayout.NORTH);
        component.setBackground(ColorPalette.popupWindowColor);
        this.pane = new LScrollPane(component);
        SmartScroller.configure(pane);
        pane.getVerticalScrollBar().setUnitIncrement(15);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setBackground(ColorPalette.popupWindowColor);
        this.add(pane, BorderLayout.CENTER);
        this.pane.setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
        this.add(input = new LHintTextField("Message..."), BorderLayout.SOUTH);
        input.setBackground(ColorPalette.popupWindowColor);
        this.input.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        this.input.addActionListener(listener -> {
            String message = input.getText();
            xmppClient.sendMessage(jid, message);
            addMessage(ChatPerspective.SELF, message);
            input.setText("");
        });
    }

    public LHintTextField getInput() {
        return input;
    }

    protected void drain() {
        synchronized (lock) {
            Dimension size = history.getSize();
            while (!queue.isEmpty()) {
                Pair<ChatPerspective, String> pair = queue.remove(0);
                ChatPerspective perspective = pair.getKey();
                String message = pair.getValue();
                Component[] components = history.getComponents();
                ChatMessage previous = null;
                if (components.length > 0) {
                    for (int i = components.length - 1; i >= 0; i--) {
                        if (components[i] instanceof ChatMessage) {
                            previous = (ChatMessage) components[i];
                            break;
                        }
                    }
                }
                if (previous != null && previous.getPerspective() == perspective) {
                    previous.append(System.lineSeparator() + message);
                } else {
                    ChatMessage component = new ChatMessage(perspective);
                    Dimension dimension = new Dimension(size.width, 10);
                    component.setPreferredSize(dimension);
                    if (previous != null) history.add(createFillComponent());
                    component.append(message);
                    history.add(component);
                }
            }
            revalidate();
            repaint();
        }
    }

    private ChildUIComponent createFillComponent() {
        ChildUIComponent filler = new ChildUIComponent(null);
        filler.setPreferredSize(new Dimension(0, 5));
        filler.setBackground(ColorPalette.popupWindowColor);
        return filler;
    }

    public void addMessage(ChatPerspective perspective, String message) {
        Dimension size = history.getSize();
        if (size.width == 0) {
            queue.add(Pair.from(perspective, message));
        } else {
            queue.add(Pair.from(perspective, message));
            synchronized (lock) {
                if (!queue.isEmpty()) {
                    drain();
                }
            }
        }
    }
}
