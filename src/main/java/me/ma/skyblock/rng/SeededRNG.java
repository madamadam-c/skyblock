package me.ma.skyblock.rng;

import java.util.Random;

public final class SeededRNG implements RNG {
    private final Random random;
    public SeededRNG(long seed) { this.random = new Random(seed); }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }
}
