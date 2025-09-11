package com.maximumg9.shadow.abilities.filters;

import com.maximumg9.shadow.abilities.Ability;
import com.maximumg9.shadow.abilities.AbilityFilterResult;

public abstract class Filter {
    public Filter(String message) {
        this.message = message;
    }

    protected final String message;

    public abstract boolean filter(Ability ability);

    public AbilityFilterResult test(Ability ability) {
        return filter(ability) ? AbilityFilterResult.PASS() : AbilityFilterResult.FAIL(this.message);
    }
}
