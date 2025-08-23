package com.maximumg9.shadow.abilities;

public final class AbilityFilterResult {
    public final Status status;
    public final String message;
    private AbilityFilterResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }
    public static AbilityFilterResult PASS() {
        return new AbilityFilterResult(Status.PASS, "");
    }
    public static AbilityFilterResult FAIL(String message) {
        return new AbilityFilterResult(Status.FAIL, message);
    }
    
    
    public enum Status {
        PASS, FAIL
    }
}