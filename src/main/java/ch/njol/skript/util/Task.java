package ch.njol.skript.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Peter Güttinger
 */
public abstract class Task implements Runnable {
	
	private final int taskID;
	
	public Task(final JavaPlugin plugin, final long delay, final long period) {
		this(plugin, delay, period, false);
	}
	
	public Task(final JavaPlugin plugin, final long delay, final long period, final boolean async) {
		if (async)
			taskID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, this, delay, period);
		else
			taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period);
	}
	
	public Task(final JavaPlugin plugin, final long delay) {
		this(plugin, delay, false);
	}
	
	public Task(final JavaPlugin plugin, final long delay, final boolean async) {
		if (async)
			taskID = Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, this, delay);
		else
			taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
	}
	
	public final boolean isAlive() {
		return Bukkit.getScheduler().isQueued(taskID) || Bukkit.getScheduler().isCurrentlyRunning(taskID);
	}
	
	public final void cancel() {
		Bukkit.getScheduler().cancelTask(taskID);
	}
	
}
