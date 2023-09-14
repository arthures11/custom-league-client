package com.hawolt.ui.generic.component;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class LScrollPane extends JScrollPane implements PropertyChangeListener {
    public LScrollPane(Component view) {
        ColorPalette.addThemeListener(this);
        getVerticalScrollBar().setUI(new LScrollBarUI());
        getHorizontalScrollBar().setUI(new LScrollBarUI());
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setViewportView(view);
        setBackground(ColorPalette.backgroundColor);
        setForeground(ColorPalette.accentColor);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setBackground(ColorPalette.getNewColor(getBackground(), (LThemeChoice) evt.getOldValue()));
        setForeground(ColorPalette.getNewColor(getForeground(), (LThemeChoice) evt.getOldValue()));
    }

    @Override
    public void updateUI() {
        super.updateUI();
        EventQueue.invokeLater(() -> {
            setComponentZOrder(getVerticalScrollBar(), 0);
            setComponentZOrder(getHorizontalScrollBar(), 1);
            setComponentZOrder(getViewport(), 2);
            getVerticalScrollBar().setOpaque(false);
            getHorizontalScrollBar().setOpaque(false);
        });
    }

    private class ScrollLayout extends ScrollPaneLayout {
        @Override
        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            if (parent instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) parent;
                Rectangle rectangle = scroll.getViewport().getBounds();
                Insets insets = parent.getInsets();
                int headerHeigth = 0;
                if (scroll.getColumnHeader() != null) {
                    Rectangle header = scroll.getColumnHeader().getBounds();
                    headerHeigth = header.height;
                }
                rectangle.width = scroll.getBounds().width - (insets.left + insets.right);
                rectangle.height = scroll.getBounds().height - (insets.top + insets.bottom) - headerHeigth;
                if (Objects.nonNull(viewport)) {
                    viewport.setBounds(rectangle);
                }
                if (!Objects.isNull(hsb)) {
                    Rectangle hrc = hsb.getBounds();
                    hrc.width = rectangle.width;
                    hsb.setBounds(hrc);
                }
            }
        }
    }
}
