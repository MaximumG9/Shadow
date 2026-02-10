package com.maximumg9.shadow.util;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class CancellableDelay extends Delay {
    private final Runnable task;
    private int timer;
    private final Supplier<Boolean> cancelCondition;

    private CancellableDelay(Runnable task, int tickDelay, Supplier<Boolean> cancelCondition) {
        super(task, tickDelay);
        this.task = task;
        this.timer = tickDelay;
        this.cancelCondition = cancelCondition;
    }

    public static CancellableDelay of(Runnable task, int tickDelay, Supplier<Boolean> cancelCondition) {
        return new CancellableDelay(task, tickDelay, cancelCondition);
    }

    @Override
    public void tick() {
        timer--;
    }

    @Override
    public boolean shouldEnd() { return timer <= 0 || cancelCondition.get(); }

    @Override
    public void onEnd() { if (!cancelCondition.get()) task.run(); }

    public static Supplier<Boolean> wrapCancelCondition(Predicate<IndirectPlayer> s, IndirectPlayer iP) {
        return () -> s.test(iP);
    }
}
