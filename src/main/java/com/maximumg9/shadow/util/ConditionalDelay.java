package com.maximumg9.shadow.util;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConditionalDelay extends Delay {
    private final Runnable task;
    private int timer;
    private final Supplier<Boolean> endCondition;

    private ConditionalDelay(Runnable task, int tickDelay, Supplier<Boolean> endCondition) {
        super(task, tickDelay);
        this.task = task;
        this.timer = tickDelay;
        this.endCondition = endCondition;
    }

    public static ConditionalDelay of(Runnable task, int tickDelay, Supplier<Boolean> endCondition) {
        return new ConditionalDelay(task, tickDelay, endCondition);
    }

    @Override
    public void tick() {
        timer--;
    }

    @Override
    public boolean shouldEnd() {
        return timer <= 0 || endCondition.get();
    }

    @Override
    public void onEnd() {
        task.run();
    }

    public static Supplier<Boolean> wrapCancelCondition(Predicate<IndirectPlayer> s, IndirectPlayer iP) {
        return () -> s.test(iP);
    }
}
