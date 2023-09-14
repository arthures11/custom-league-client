package com.hawolt.ui.generic.component;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.util.paint.PaintHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.util.HashMap;

public class LTabbedPane extends JTabbedPane {
    private static final Font regular = new Font("Dialog", Font.BOLD, 18);
    HashMap<Integer, Boolean> tabHoverMap = new HashMap<Integer, Boolean>();

    public LTabbedPane() {
        super();
        init(regular);
    }

    public LTabbedPane(Font font) {
        init(font);
    }

    public LTabbedPane(int tabPlacement) {
        super(tabPlacement);
        init(regular);
    }

    public LTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
        init(regular);
    }

    private void init(Font font) {
        setBackground(ColorPalette.backgroundColor);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setUI(new LTabbedUI(HighlightType.BOTTOM));
        setFont(font);
        setForeground(ColorPalette.textColor);

        //Look for better solutions to find out if mouse is hovering tab
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (int i = 0; i < getTabCount(); i++) {
                    tabHoverMap.put(i, getBoundsAt(i).contains(e.getPoint()) && isEnabledAt(i));
                    repaint();
                }
            }
        });
    }

    public class LTabbedUI extends MetalTabbedPaneUI {

        private HighlightType highlightType;
        private int selectedIndicatorSize = 5;

        public LTabbedUI(HighlightType highlight) {
            highlightType = highlight;
        }

        public void setSelectionIndicatorSize(int size) {
            selectedIndicatorSize = size;
        }

        @Override
        public void installUI(JComponent jc) {
            super.installUI(jc);
        }

        @Override
        protected void paintTabBorder(Graphics grphcs, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2d = (Graphics2D) grphcs.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected || (tabHoverMap.containsKey(tabIndex) && tabHoverMap.get(tabIndex))) {
                switch (highlightType) {
                    case LEFT:
                        w = selectedIndicatorSize;
                        break;
                    case TOP:
                        h = selectedIndicatorSize;
                        break;
                    case RIGHT:
                        x = w - selectedIndicatorSize;
                        w = selectedIndicatorSize;
                        break;
                    case BOTTOM:
                        y = h - selectedIndicatorSize;
                        h = selectedIndicatorSize;
                        break;
                }
                Area buttonArea = new Area(new Rectangle(x, y, w, h));
                g2d.setColor(ColorPalette.buttonSelectionColor);
                g2d.fill(buttonArea);
            }
            g2d.dispose();
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement,
                                 Font font, FontMetrics metrics, int tabIndex,
                                 String title, Rectangle textRect,
                                 boolean isSelected) {

            g.setFont(font);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            View v = getTextViewForTab(tabIndex);
            if (v != null) {
                // html
                v.paint(g, textRect);
            } else {
                // plain text
                int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);

                if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
                    Color fg = tabPane.getForegroundAt(tabIndex);
                    if (isSelected && (fg instanceof UIResource)) {
                        Color selectedFG = UIManager.getColor(
                                "TabbedPane.selectedForeground");
                        if (selectedFG != null) {
                            fg = selectedFG;
                        }
                    }
                    PaintHelper.drawShadowText(g2d, title, textRect.x, textRect.y + PaintHelper.getFontHeight(metrics), fg);

                } else { // tab disabled
                    PaintHelper.drawShadowText(g2d, title, textRect.x, textRect.y + PaintHelper.getFontHeight(metrics), getForegroundAt(tabIndex).darker());
                }
            }
        }

        //Sets button insets
        @Override
        protected Insets getTabInsets(int i, int i1) {
            return new Insets(10, 10, 10, 10);
        }

        @Override
        protected void paintContentBorder(Graphics grphcs, int tabPlacement, int selectedIndex) {
            //empty so that the border is not drawn
        }

        @Override
        protected void paintFocusIndicator(Graphics grphcs, int i, Rectangle[] rctngls, int i1, Rectangle rctngl, Rectangle rctngl1, boolean bln) {

        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            if (tabPane.isOpaque()) {
                super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
            }
        }
    }
}
