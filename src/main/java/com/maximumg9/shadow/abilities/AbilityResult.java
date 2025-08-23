package com.maximumg9.shadow.abilities;

public enum AbilityResult {
    NO_CLOSE(false,false),
    CLOSE(true,false),
    NO_CLOSE_AND_RESET(false,true),
    CLOSE_AND_RESET(true,true);
    
    public final boolean close;
    public final boolean reset;

    AbilityResult(boolean close, boolean reset) {
        this.close = close;
        this.reset = reset;
    }
}
