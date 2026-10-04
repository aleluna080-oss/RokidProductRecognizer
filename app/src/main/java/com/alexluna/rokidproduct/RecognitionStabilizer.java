package com.alexluna.rokidproduct;

/** Three consecutive category matches; expire old results after 2.5 seconds. */
public final class RecognitionStabilizer {
    private String pending = "";
    private int hits;
    private long lastConfirmedAt;
    private boolean confirmed;

    public boolean accept(String category, long now) {
        if (category == null || category.isEmpty()) {
            pending = "";
            hits = 0;
            return false;
        }
        if (category.equals(pending)) hits = Math.min(3, hits + 1);
        else { pending = category; hits = 1; }
        if (hits < 3) return false;
        confirmed = true;
        lastConfirmedAt = now;
        return true;
    }

    public boolean expire(long now) {
        if (!confirmed || now - lastConfirmedAt < 2500L) return false;
        reset();
        return true;
    }

    public void reset() {
        pending = "";
        hits = 0;
        confirmed = false;
    }
}
