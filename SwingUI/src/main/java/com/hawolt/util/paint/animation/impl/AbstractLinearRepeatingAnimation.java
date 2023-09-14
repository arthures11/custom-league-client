package com.hawolt.util.paint.animation.impl;

import com.hawolt.util.paint.animation.Animation;

import java.awt.*;
import java.util.concurrent.TimeUnit;

/**
 * Created: 14/09/2023 15:56
 * Author: Twitter @hawolt
 **/

public abstract class AbstractLinearRepeatingAnimation implements Animation {
    private final float animationSpeed;
    private final long tickSpeed;
    private final double rate;
    private final short fps;
    private long timestamp = System.currentTimeMillis();

    public AbstractLinearRepeatingAnimation() {
        this(1f);
    }

    public AbstractLinearRepeatingAnimation(float animationSpeed) {
        this(animationSpeed, Animation.getMonitorRefreshRate());
    }

    public AbstractLinearRepeatingAnimation(float animationSpeed, short fps) {
        this.rate = TimeUnit.SECONDS.toMillis((long) (getAnimationDuration() / animationSpeed)) / (double) getAnimationSteps();
        this.tickSpeed = (int) Math.round(1000D / fps);
        this.animationSpeed = animationSpeed;
        this.fps = fps;
    }

    @Override
    public long getRefreshRate() {
        return tickSpeed;
    }

    @Override
    public short getTargetFPS() {
        return fps;
    }

    @Override
    public float getAnimationSpeed() {
        return animationSpeed;
    }

    @Override
    public void interpolate() {
        long timestamp = System.currentTimeMillis();
        double timeSinceLastUpdate = timestamp - this.timestamp;
        this.interpolate(1 * (timeSinceLastUpdate / rate));
        this.timestamp = timestamp;

    }

    public abstract Color getCanvasColor();

    public abstract Color getForegroundColor();

    public abstract int getAnimationSteps();

    public abstract void interpolate(double factor);
}
