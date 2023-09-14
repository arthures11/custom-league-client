package com.hawolt.util.paint.animation;

import javax.swing.*;
import java.awt.*;

/**
 * Created: 14/09/2023 13:50
 * Author: Twitter @hawolt
 **/

public interface Animation {
    static short getRefreshRate(Frame frame) {
        GraphicsDevice device = frame.getGraphicsConfiguration().getDevice();
        return (short) device.getDisplayMode().getRefreshRate();
    }

    static short getMonitorRefreshRate() {
        return getRefreshRate(JFrame.getFrames()[0]);
    }

    default void animate(Rectangle area, Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        animate(area, graphics2D);
    }

    void animate(Rectangle area, Graphics2D graphics2D);

    int getAnimationDuration();

    float getAnimationSpeed();

    long getRefreshRate();

    short getTargetFPS();

    void interpolate();
}
