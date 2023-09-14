package com.hawolt.ui.settings;

import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LLabel;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class SettingsSidebar extends JPanel {
    private static final Font headerFont = new Font("Arial", Font.BOLD, 20);
    private static final Dimension sidebarDimension = new Dimension(200, 0);
    private static final Dimension headerDimension = new Dimension(190, 40);
    private static final Dimension sectionDimension = new Dimension(190, 40);
    ChildUIComponent panel;

    public SettingsSidebar() {
        super();
        this.setLayout(new BorderLayout());
        this.setPreferredSize(sidebarDimension);

        panel = new ChildUIComponent();
        panel.setBackground(ColorPalette.accentColor);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
        this.add(panel);
    }

    protected static LFlatButton newSectionButton(String name, CardLayout cl, JPanel main) {
        LFlatButton button = new LFlatButton(name, LTextAlign.CENTER, HighlightType.LEFT);
        button.setActionCommand(name);
        button.setBackground(ColorPalette.accentColor);
        button.setMaximumSize(sectionDimension);

        button.addActionListener(listener -> {
            cl.show(main, listener.getActionCommand());
        });

        return button;
    }

    protected GroupTab addGroupTab(String name) {
        GroupTab result = new GroupTab(name);
        result.setBackground(ColorPalette.accentColor);
        panel.add(result);
        return result;
    }

    protected class GroupTab extends ChildUIComponent {
        private JPanel container;

        private GroupTab(String name) {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            container = new ChildUIComponent();
            container.setBackground(ColorPalette.accentColor);
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setPreferredSize(sectionDimension);
            container.setBorder(
                    BorderFactory.createTitledBorder(
                            new MatteBorder(2, 2, 2, 2, Color.DARK_GRAY)
                    )
            );

            LLabel header = new LLabel(name, LTextAlign.LEFT);
            header.setFont(headerFont);
            header.setMaximumSize(headerDimension);

            this.add(header);
            this.add(container);
        }

        protected Component addToContainer(Component component) {
            container.add(component);
            return component;
        }
    }
}
