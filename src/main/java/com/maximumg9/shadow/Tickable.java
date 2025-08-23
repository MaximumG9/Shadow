package com.maximumg9.shadow;

public interface Tickable {
    void tick();
    
    default void onEnd() { }
    
    default boolean shouldEnd() { return false; }
}
