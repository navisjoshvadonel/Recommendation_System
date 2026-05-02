package com.companion.navis;

import com.companion.gokhul.Item;

public class ScoredResult implements Comparable<ScoredResult> {
    private Item item;
    private double score;

    public ScoredResult(Item item, double score) {
        this.item = item;
        this.score = score;
    }

    public Item getItem() { return item; }
    public double getScore() { return score; }

    @Override
    public int compareTo(ScoredResult o) {
        // Higher score ranks first
        return Double.compare(o.score, this.score);
    }
}
