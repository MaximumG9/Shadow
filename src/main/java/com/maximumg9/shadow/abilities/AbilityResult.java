package com.maximumg9.shadow.abilities;

public enum AbilityResult {
    NO_CLOSE(false),
    CLOSE(true);
    
    public final boolean close;

    AbilityResult(boolean close) {
        this.close = close;
    }
}
