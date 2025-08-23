package com.maximumg9.shadow.util.indirectplayer;

import com.maximumg9.shadow.GamePhase;

import java.util.function.Predicate;

import static com.maximumg9.shadow.util.MiscUtil.getShadow;

public abstract class CancelPredicates {
    public static final Predicate<IndirectPlayer> IS_NIGHT = (p) -> getShadow(p.server).isNight();
    public static final Predicate<IndirectPlayer> IS_DAY = (p) -> !getShadow(p.server).isNight();
    public static final Predicate<IndirectPlayer> NEVER_CANCEL = (p) -> false;
    public static Predicate<IndirectPlayer> cancelOnPhaseChange(GamePhase currentPhase) {
        return (p) -> currentPhase != getShadow(p.server).state.phase;
    }
}
