package com.hawolt.ui.generic.component;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.themes.impl.LThemeChoice;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.util.paint.PaintHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LFlatButton extends JButton implements PropertyChangeListener {
    private int selectedIndicatorSize = 5;

    private boolean showSelectionIndicator;
    private HighlightType highlightType;

    private LTextAlign textAlign;

    private Color highlightColor = ColorPalette.buttonSelectionColor;

    private int rounding = 0;

    private boolean roundTL, roundTR, roundBL, roundBR, customRoundArea;

    public LFlatButton() {
        init();
        textAlign = LTextAlign.LEFT;
    }

    public LFlatButton(String text, LTextAlign textAlign) {
        super(text);
        init();
        this.textAlign = textAlign;
        highlightType = HighlightType.LEFT;
    }

    public LFlatButton(String text, LTextAlign textAlign, HighlightType highlightType) {
        super(text);
        this.highlightType = highlightType;
        init();
        this.textAlign = textAlign;
    }

    protected void init() {
        ColorPalette.addThemeListener(this);
        setContentAreaFilled(false);
        setFocusPainted(false); //Disable text border on focus
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setForeground(ColorPalette.textColor); //Set text to white

        setBackground(new Color(0, 0, 0, 0)); //Set default background color

        setFont(new Font("Dialog", Font.BOLD, 18));

        //Change color on mouse press
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                showSelectionIndicator = true;
            }

            public void mouseEntered(MouseEvent evt) {
                if (highlightType == HighlightType.TEXT)
                    setForeground(highlightColor);
                showSelectionIndicator = true;
                repaint();
            }

            public void mouseExited(MouseEvent evt) {
                if (isSelected())
                    showSelectionIndicator = true;
                else {
                    setForeground(ColorPalette.textColor);
                    showSelectionIndicator = false;
                }
                repaint();
            }
        });

        //Change color on button selection
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (isSelected()) {
                    if (highlightType == HighlightType.TEXT)
                        setForeground(highlightColor);
                    showSelectionIndicator = true;
                } else {
                    setForeground(ColorPalette.textColor);
                    showSelectionIndicator = false;
                }
            }
        });
    }

    public void setRounding(int rounding) {
        this.rounding = rounding;
    }

    public void setRoundingCorners(boolean tl, boolean tr, boolean bl, boolean br) {
        roundTL = tl;
        roundTR = tr;
        roundBL = bl;
        roundBR = br;
        customRoundArea = tl || tr || bl || br;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LThemeChoice old = (LThemeChoice) evt.getOldValue();
        setBackground(ColorPalette.getNewColor(getBackground(), old));

        highlightColor = ColorPalette.getNewColor(highlightColor, old);

        setForeground(ColorPalette.getNewColor(getForeground(), old));
    }

    public void setHighlightType(HighlightType direction) {
        highlightType = direction;
    }

    public void setHighlightColor(Color color) {
        highlightColor = color;
    }

    public void setSelectionIndicatorSize(int size) {
        selectedIndicatorSize = size;
    }

    public void setTextAlign(LTextAlign align) {
        textAlign = align;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        int x = 0, y = 0, width = getWidth(), height = getHeight();
        if (!customRoundArea)
            g2d.fillRoundRect(0, 0, width, height, ColorPalette.useRoundedCorners ? rounding : 0, ColorPalette.useRoundedCorners ? rounding : 0);
        else
            PaintHelper.roundedSquare(g2d, 0, 0, getWidth(), getHeight(), rounding, roundTR, roundTL, roundBR, roundBL);

        switch (highlightType) {
            case LEFT:
                width = selectedIndicatorSize;
                break;
            case TOP:
                height = selectedIndicatorSize;
                break;
            case RIGHT:
                x = width - selectedIndicatorSize;
                width = selectedIndicatorSize;
                break;
            case BOTTOM:
                y = height - selectedIndicatorSize;
                height = selectedIndicatorSize;
                break;
            case TEXT:
                width = 0;
                height = 0;
        }
        Area buttonArea = new Area(new RoundRectangle2D.Double(x, y, width, height,
                ColorPalette.useRoundedCorners && highlightType == HighlightType.COMPONENT ? rounding : 0,
                ColorPalette.useRoundedCorners && highlightType == HighlightType.COMPONENT ? rounding : 0));
        g2d.setColor(highlightColor);
        if (showSelectionIndicator && isEnabled()) {
            if (!customRoundArea)
                g2d.fill(buttonArea);
            else
                PaintHelper.roundedSquare(g2d, 0, 0, getWidth(), getHeight(), rounding, roundTR, roundTL, roundBR, roundBL);
        }

        g2d.setFont(getFont());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics metrics = getFontMetrics(getFont());
        String text = getText();
        switch (textAlign) {
            case LEFT:
                if (highlightType != HighlightType.LEFT)
                    x = getWidth() / 20;
                else
                    x = getWidth() / 20 + selectedIndicatorSize;
                break;
            case CENTER:
                x = getWidth() / 2 - metrics.stringWidth(text) / 2;
                break;
            case RIGHT:
                if (highlightType != HighlightType.RIGHT)
                    x = getWidth() - metrics.stringWidth(text) - getWidth() / 20;
                else
                    x = getWidth() - metrics.stringWidth(text) - getWidth() / 20 - selectedIndicatorSize;
                break;
        }
        y = getHeight() / 2 + (metrics.getAscent() - metrics.getDescent() - metrics.getLeading()) / 2;
        PaintHelper.drawShadowText(g2d, text, x, y, isEnabled() ? getForeground() : getForeground().darker());


        //  super.paintComponent(graphics);
    }
}