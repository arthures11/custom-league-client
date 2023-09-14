package com.hawolt.ui.github;

import com.hawolt.generic.util.Network;
import com.hawolt.logger.Logger;
import com.hawolt.ui.generic.component.LScrollPane;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ReleaseWindow extends ChildUIComponent implements PropertyChangeListener {
    private final JTextPane changelog;

    public ReleaseWindow() {
        super(new BorderLayout());

        //TODO Make custom jtextpane
        LScrollPane pane = new LScrollPane(changelog = new JTextPane());
        changelog.setBackground(ColorPalette.backgroundColor);

        StyledDocument document = changelog.getStyledDocument();
        Style style = changelog.addStyle("", null);
        StyleConstants.setForeground(style, Color.WHITE);
        changelog.setFont(new Font("Dialog", Font.BOLD, 14));

        changelog.setEditable(false);
        changelog.setContentType("text/html");
        changelog.setText(createTemplate());
        changelog.addHyperlinkListener(this::handleLink);

        add(pane);
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    public String createTemplate() {
        HtmlBuilder builder = new HtmlBuilder();

        for (String line : Github.getChangelog().split("\r")) {
            for (String word : line.split(" ")) {
                builder.add(word);
            }
            builder.endLine();
        }

        // add discord link for good measures
        builder.endLine().addLink("https://discord.gg/UcGhC9dcHk", "[Click] Join the Discord for more stuff");

        return builder.build();
    }

    private void handleLink(HyperlinkEvent e) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
            try {
                Network.browse(e.getURL().toString());
            } catch (Exception ex) {
                Logger.error(ex);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        changelog.setBackground(ColorPalette.backgroundColor);
    }
}
