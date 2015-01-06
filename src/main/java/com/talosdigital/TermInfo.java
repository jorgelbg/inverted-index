package com.talosdigital;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TermInfo extends HashMap {
    private List<Integer> positions;
    private int freq;

    public TermInfo(int pos) {
        this.freq = 1;
        this.positions = new ArrayList<>();

        this.positions.add(pos);
    }

    public TermInfo() {
        this.freq = 0;
        this.positions = new ArrayList<>();
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public void addPosition(int pos) {
        this.positions.add(pos);
        this.freq++;
    }
}
