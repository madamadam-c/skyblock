package me.ma.skyblock.tick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class TickEngine {
    private final JavaPlugin plugin;
    private final List<ScheduledTask> tasks = new ArrayList<>();
    private long currentTick;
    private BukkitTask runner;

    public TickEngine(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (runner != null) {
            return;
        }

        runner = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (runner != null) {
            runner.cancel();
            runner = null;
        }
        tasks.clear();
    }

    public void runEvery(int periodTicks, Runnable task) {
        if (periodTicks <= 0) {
            throw new IllegalArgumentException("periodTicks must be > 0");
        }
        tasks.add(new ScheduledTask(task, periodTicks, currentTick + periodTicks, true));
    }

    public void scheduleIn(int delayTicks, Runnable task) {
        if (delayTicks < 0) {
            throw new IllegalArgumentException("delayTicks must be >= 0");
        }
        tasks.add(new ScheduledTask(task, delayTicks, currentTick + delayTicks, false));
    }

    private void tick() {
        currentTick++;
        Iterator<ScheduledTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            if (currentTick < task.nextRunTick) {
                continue;
            }
            task.task.run();
            if (task.recurring) {
                task.nextRunTick = currentTick + task.periodTicks;
            } else {
                iterator.remove();
            }
        }
    }

    private static final class ScheduledTask {
        private final Runnable task;
        private final int periodTicks;
        private long nextRunTick;
        private final boolean recurring;

        private ScheduledTask(Runnable task, int periodTicks, long nextRunTick, boolean recurring) {
            this.task = task;
            this.periodTicks = periodTicks;
            this.nextRunTick = nextRunTick;
            this.recurring = recurring;
        }
    }
}
