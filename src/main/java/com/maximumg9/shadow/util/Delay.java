package com.maximumg9.shadow.util;

import com.maximumg9.shadow.Tickable;

public class  Delay implements Tickable {
    private final Runnable task;
    private int timer;
    
    private Delay(Runnable task, int tickDelay) {
        this.task = task;
        this.timer = tickDelay;
    }
    
    public static Delay of(Runnable task, int tickDelay) {
        return new Delay(task, tickDelay);
    }
    
    public static Delay instant(Runnable task) {
        return new Delay(task, 0);
    }
    
    @Override
    public void tick() {
        timer--;
    }
    
    @Override
    public boolean shouldEnd() {
        return timer <= 0;
    }
    
    @Override
    public void onEnd() {
        task.run();
    }
}
