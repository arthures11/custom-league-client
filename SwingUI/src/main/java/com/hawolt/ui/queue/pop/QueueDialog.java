package com.hawolt.ui.queue.pop;

import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created: 11/08/2023 21:31
 * Author: Twitter @hawolt
 **/

public class QueueDialog extends JDialog implements ActionListener {
    private int selection;

    public QueueDialog(Frame frame, String title, long maxAfkMillis) {
        super(frame, title);
        ChildUIComponent container = new ChildUIComponent(new BorderLayout());
        container.setBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(1, 1, 1, 1, Color.DARK_GRAY),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
        setContentPane(container);

        container.setLayout(new BorderLayout());
        container.setPreferredSize(new Dimension(200, 75));

        QueueCountdown countdown = new QueueCountdown(this, maxAfkMillis);
        container.add(countdown, BorderLayout.CENTER);

        ChildUIComponent component = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        component.setBorder(new EmptyBorder(5, 0, 0, 0));

        LFlatButton accept = new LFlatButton("Accept", LTextAlign.CENTER, HighlightType.COMPONENT);
        accept.addActionListener(this);
        component.add(accept);

        LFlatButton decline = new LFlatButton("Decline", LTextAlign.CENTER, HighlightType.COMPONENT);
        decline.addActionListener(this);
        component.add(decline);

        container.add(component, BorderLayout.SOUTH);
        setUndecorated(true);
        setResizable(false);
        pack();
        setLocationRelativeTo(frame);
    }

    public QueueDialog showQueueDialog() {
        setModal(true);
        setVisible(true);
        return this;
    }

    public int getSelection() {
        return selection;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Accept" -> selection = 1;
            case "Decline" -> selection = 2;
        }
        dispose();
    }
}
