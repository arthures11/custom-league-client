package com.hawolt.ui.champselect.generic;

import com.hawolt.client.resources.communitydragon.rune.BasicRune;
import com.hawolt.ui.champselect.IRuneSelection;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.LazyLoadedImageComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;

/**
 * Created: 03/09/2023 01:54
 * Author: Twitter @hawolt
 **/

public class ChampSelectionRunePanel extends LazyLoadedImageComponent implements MouseListener {
    private final IRuneSelection selection;
    private final int componentIndex;
    private final BasicRune rune;
    private boolean selected;

    public ChampSelectionRunePanel(IRuneSelection selection, int componentIndex, BasicRune rune, Dimension dimension) {
        super(rune.getIconPath(), dimension);
        this.rune = rune;
        this.selection = selection;
        this.addMouseListener(this);
        this.componentIndex = componentIndex;
    }

    public BasicRune getRune() {
        return rune;
    }

    public int getComponentIndex() {
        return componentIndex;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.selected = !selected;
        selection.onSelection(componentIndex, selected);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Dimension dimension = getSize();
        g.setColor(ColorPalette.backgroundColor);
        g.fillRect(0, 0, dimension.width, dimension.height);
        Graphics2D graphics2D = (Graphics2D) g;
        if (selected && image != null) {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(ColorPalette.buttonSelectionColor);
            int arcWidth = (image.getWidth() + 10);
            int arcHeigth = (image.getHeight() + 10);
            int arcX = (dimension.width >> 1) - (arcWidth >> 1);
            int arcY = (dimension.height >> 1) - (arcHeigth >> 1);
            graphics2D.fill(new RoundRectangle2D.Float(arcX, arcY, arcWidth, arcHeigth, 360, 360));
        }
        if (image == null) return;
        int imageX = (dimension.width >> 1) - (image.getWidth() >> 1);
        int imageY = (dimension.height >> 1) - (image.getHeight() >> 1);
        if (selected) {
            g.drawImage(image, imageX, imageY, null);
        } else {
            ImageFilter filter = new GrayFilter(true, 25);
            ImageProducer producer = new FilteredImageSource(image.getSource(), filter);
            Image custom = Toolkit.getDefaultToolkit().createImage(producer);
            g.drawImage(custom, imageX, imageY, null);
        }
    }
}