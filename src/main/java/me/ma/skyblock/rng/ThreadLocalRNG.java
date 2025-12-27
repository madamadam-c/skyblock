package me.ma.skyblock.rng;

import java.util.concurrent.ThreadLocalRandom;

public final class ThreadLocalRNG implements RNG {
    @Override
    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }
}
