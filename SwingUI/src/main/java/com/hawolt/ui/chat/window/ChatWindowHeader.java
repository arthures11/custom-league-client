package com.hawolt.ui.chat.window;

import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LLabel;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.util.paint.PaintHelper;

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 08/08/2023 20:51
 * Author: Twitter @hawolt
 **/

public class ChatWindowHeader extends ChildUIComponent {

    private final LLabel target;

    public ChatWindowHeader(LayoutManager layout) {
        super(layout);
        this.setBackground(ColorPalette.accentColor);
        this.setForeground(ColorPalette.textColor);
        this.setBorder(new EmptyBorder(0, 5, 0, 0));
        this.add(target = new LLabel("", LTextAlign.LEFT), BorderLayout.CENTER);
        LFlatButton close = new LFlatButton("×", LTextAlign.CENTER, HighlightType.COMPONENT);
        close.setRoundingCorners(true, false, true, false);
        close.setRounding(ColorPalette.CARD_ROUNDING);
        this.add(close, BorderLayout.EAST);
        close.addActionListener(listener -> {
            this.getParent().setVisible(false);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //Drawing top left rounding
        int width = getWidth();
        int height = getHeight();
        g2d.setColor(ColorPalette.backgroundColor);
        g2d.fillRect(0, 0, ColorPalette.CARD_ROUNDING, ColorPalette.CARD_ROUNDING);
        g2d.setColor(ColorPalette.accentColor);
        PaintHelper.roundedSquare(g2d, 0, 0, width, height, ColorPalette.CARD_ROUNDING, false, true, false, false);
        g2d.dispose();
    }

    public void setTarget(String name) {
        target.setText(name);
    }

}
