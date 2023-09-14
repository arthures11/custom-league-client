package com.hawolt.util.paint.animation;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created: 14/09/2023 14:05
 * Author: Twitter @hawolt
 **/

public class AnimationVisualizer extends JComponent {
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final Animation animation;
    private ScheduledFuture<?> future;

    public AnimationVisualizer(Animation animation) {
        this.animation = animation;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle area = getBounds();
        this.animation.animate(area, g);
    }

    public boolean isRunning() {
        return future != null && !future.isCancelled();
    }

    public void start() {
        if (isRunning()) {
            throw new RuntimeException("Animation is already running");
        } else {
            future = service.scheduleAtFixedRate(() -> {
                this.animation.interpolate();
                this.repaint();
            }, 0, animation.getRefreshRate(), TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        if (future == null) return;
        future.cancel(true);
    }
}
