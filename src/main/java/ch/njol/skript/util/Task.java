package ch.njol.skript.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.Skript;

/**
 * @author Peter Güttinger
 */
public abstract class Task implements Runnable {
	
	private final JavaPlugin plugin;
	private final boolean async;
	private long period = -1;
	
	private int taskID;
	
	public Task(final JavaPlugin plugin, final long delay, final long period) {
		this(plugin, delay, period, false);
	}
	
	public Task(final JavaPlugin plugin, final long delay, final long period, final boolean async) {
		this.plugin = plugin;
		this.period = period;
		this.async = async;
		schedule(delay);
	}
	
	public Task(final JavaPlugin plugin, final long delay) {
		this(plugin, delay, false);
	}
	
	public Task(final JavaPlugin plugin, final long delay, final boolean async) {
		this.plugin = plugin;
		this.async = async;
		schedule(delay);
	}
	
	@SuppressWarnings("deprecation")
	private void schedule(final long delay) {
		if (period == -1) {
			if (async) {
				taskID = Skript.isRunningMinecraft(1, 4, 6) ?
						Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, delay).getTaskId() :
						Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, this, delay);
			} else {
				taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
			}
		} else {
			if (async) {
				taskID = Skript.isRunningMinecraft(1, 4, 6) ?
						Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, delay, period).getTaskId() :
						Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, this, delay, period);
			} else {
				taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period);
			}
		}
	}
	
	public final boolean isAlive() {
		return Bukkit.getScheduler().isQueued(taskID) || Bukkit.getScheduler().isCurrentlyRunning(taskID);
	}
	
	public final void cancel() {
		Bukkit.getScheduler().cancelTask(taskID);
	}
	
	public void setNextExecution(final long delay) {
		assert delay >= 0;
		Bukkit.getScheduler().cancelTask(taskID);
		schedule(delay);
	}
	
	public void setPeriod(final long period) {
		assert period == -1 || period > 0;
		if (period == this.period)
			return;
		Bukkit.getScheduler().cancelTask(taskID);
		this.period = period;
		schedule(period);
	}
	
	/**
	 * Calls a method on Bukkit's main thread.
	 * <p>
	 * Hint: Use a Callable&lt;Void&gt; to make a task which blocks your current thread until it is completed.
	 * 
	 * @param c The method
	 * @return What the method returned or null if it threw an error or was stopped (usually due to the server shutting down)
	 */
	public final static <T> T callSync(final Callable<T> c) {
		if (Bukkit.isPrimaryThread()) {
			try {
				return c.call();
			} catch (final Exception e) {
				Skript.exception(e);
			}
		}
		final Future<T> f = Bukkit.getScheduler().callSyncMethod(Skript.getInstance(), c);
		try {
			while (true) {
				try {
					return f.get();
				} catch (final InterruptedException e) {}
			}
		} catch (final ExecutionException e) {
			Skript.exception(e);
		} catch (final CancellationException e) {} catch (final ThreadDeath e) {}// server shutting down
		return null;
	}
	
}
