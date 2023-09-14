package com.hawolt.util.paint.animation.impl.impl;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.util.paint.animation.Animation;
import com.hawolt.util.paint.animation.impl.AbstractLinearRepeatingAnimation;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * Created: 14/09/2023 13:51
 * Author: Twitter @hawolt
 **/

public class SpinningAnimation extends AbstractLinearRepeatingAnimation {
    private static final double MAX_ARC_VALUE = 360;
    private final Color background = ColorPalette.backgroundColor, foreground = Color.WHITE;
    private final int fragments, extend;
    private final int[] offsets;
    private double animationIndex = MAX_ARC_VALUE;

    public SpinningAnimation(int fragments, int extend) {
        this(fragments, extend, 1f);
    }

    public SpinningAnimation(int fragments, int extend, float animationSpeed) {
        this(fragments, extend, animationSpeed, Animation.getMonitorRefreshRate());
    }

    public SpinningAnimation(int fragments, int extend, float animationSpeed, short fps) {
        super(animationSpeed, fps);
        this.extend = extend;
        this.fragments = fragments;
        this.offsets = new int[fragments];
    }

    @Override
    public void animate(Rectangle area, Graphics2D graphics2D) {
        graphics2D.setColor(background);
        graphics2D.fillRect(area.x, area.y, area.width, area.height);
        Dimension dimension = new Dimension(area.width, area.height);
        int halfX = dimension.width >> 1, halfY = dimension.height >> 1, quarterY = halfY >> 1, mini = quarterY >> 1, nano = mini >> 1;
        Shape inner = new Ellipse2D.Double(halfX - quarterY - nano, quarterY - nano, halfY + mini, halfY + mini);
        Area center = new Area(inner), animation = new Area();
        int[] fragmentOffsets = getFragmentOffset();
        for (int fragmentOffset : fragmentOffsets) {
            animation.add(
                    new Area(
                            new Arc2D.Double(
                                    halfX - quarterY - mini,
                                    quarterY - mini,
                                    halfY + quarterY,
                                    halfY + quarterY,
                                    fragmentOffset,
                                    extend,
                                    Arc2D.PIE
                            )
                    )
            );
        }
        animation.subtract(center);
        graphics2D.setColor(foreground);
        graphics2D.fill(animation);
    }

    public int[] getFragmentOffset() {
        return offsets;
    }

    @Override
    public int getAnimationDuration() {
        return 5;
    }

    @Override
    public Color getCanvasColor() {
        return background;
    }

    @Override
    public Color getForegroundColor() {
        return foreground;
    }

    @Override
    public int getAnimationSteps() {
        return (int) MAX_ARC_VALUE;
    }

    @Override
    public void interpolate(double factor) {
        this.animationIndex = (animationIndex % MAX_ARC_VALUE) + factor;
        int spacing = (int) (MAX_ARC_VALUE / getFragmentOffset().length);
        for (int i = 0; i < fragments; i++) {
            offsets[i] = ((int) Math.ceil(animationIndex)) + (i * spacing);
        }
    }
}
