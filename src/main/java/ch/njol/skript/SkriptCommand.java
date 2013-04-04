package ch.njol.skript;

import static ch.njol.skript.Skript.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import ch.njol.skript.Updater.UpdateState;
import ch.njol.skript.Updater.VersionInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.CommandHelp;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

/**
 * @author Peter Güttinger
 */
public class SkriptCommand implements CommandExecutor {
	
	private final static CommandHelp skriptCommandHelp = new CommandHelp("<gray>/<gold>skript", "cyan", "skript command")
			.add(new CommandHelp("reload", "red")
					.add("all")
					.add("config")
					.add("aliases")
					.add("scripts")
					.add("<script>")
			).add(new CommandHelp("enable", "red")
					.add("all")
					.add("<script>")
			).add(new CommandHelp("disable", "red")
					.add("all")
					.add("<script>")
			).add(new CommandHelp("update", "red")
					.add("check")
					.add("changes")
					.add("download")
			//			).add(new CommandHelp("variable", "Commands for modifying variables", "red")
//					.add("set", "Creates a new variable or changes an existing one")
//					.add("delete", "Deletes a variable")
//					.add("find", "Find variables")
			).add("help");
	
	private final static void reloading(final CommandSender sender, final String what) {
		Skript.info(sender, "Reloading " + what + "...");
	}
	
	private final static void reloaded(final CommandSender sender, final RedirectingLogHandler r, final String what) {
		if (r.numErrors() == 0)
			Skript.info(sender, "Successfully reloaded " + what + ".");
		else
			Skript.error(sender, "Encountered " + r.numErrors() + " error" + (r.numErrors() == 1 ? "" : "s") + " while reloading " + what + "!");
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!skriptCommandHelp.test(sender, args))
			return true;
		final RedirectingLogHandler r = SkriptLogger.startLogHandler(new RedirectingLogHandler(sender, ""));
		try {
			if (args[0].equalsIgnoreCase("reload")) {
				if (args[1].equalsIgnoreCase("all")) {
					reloading(sender, "the config and all scripts");
					reload();
					reloaded(sender, r, "the config and all scripts");
				} else if (args[1].equalsIgnoreCase("scripts")) {
					reloading(sender, "all scripts");
					reloadScripts();
					reloaded(sender, r, "all scripts");
				} else if (args[1].equalsIgnoreCase("config")) {
					reloading(sender, "the main configuration");
					reloadMainConfig();
					reloaded(sender, r, "the main configuration");
				} else if (args[1].equalsIgnoreCase("aliases")) {
					reloading(sender, "the aliases");
					reloadAliases();
					reloaded(sender, r, "the aliases");
				} else {
					final File f = getScriptFromArgs(sender, args, 1);
					if (f == null)
						return true;
					if (f.getName().startsWith("-")) {
						info(sender, "This script is currently disabled. Use <gray>/<gold>skript <cyan>enable <red>" + StringUtils.join(args, " ", 1, args.length) + "<reset> to enable it.");
						return true;
					}
					reloading(sender, "<gold>" + f.getName() + "<reset>");
					ScriptLoader.unloadScript(f);
					ScriptLoader.loadScripts(new File[] {f});
					reloaded(sender, r, "<gold>" + f.getName() + "<reset>");
				}
			} else if (args[0].equalsIgnoreCase("enable")) {
				if (args[1].equals("all")) {
					try {
						info(sender, "Enabling all disabled scripts...");
						final Collection<File> files = toggleScripts(true);
						ScriptLoader.loadScripts(files.toArray(new File[0]));
						if (r.numErrors() == 0) {
							info(sender, "Successfully enabled & parsed all previously disabled scripts.");
						} else {
							Skript.error(sender, "Encountered " + r.numErrors() + " error" + (r.numErrors() == 1 ? "" : "s") + " while parsing disabled scripts!");
						}
					} catch (final IOException e) {
						error(sender, "Could not load any scripts (some scripts might have been renamed already and will be enabled when the server restarts): " + e.getLocalizedMessage());
					}
				} else {
					File f = getScriptFromArgs(sender, args, 1);
					if (f == null)
						return true;
					if (!f.getName().startsWith("-")) {
						info(sender, "<gold>" + f.getName() + "<reset> is already enabled! Use <gray>/<gold>skript <cyan>reload <red>" + StringUtils.join(args, " ", 1, args.length) + "<reset> to reload it if it was changed.");
						return true;
					}
					
					try {
						FileUtils.move(f, new File(f.getParentFile(), f.getName().substring(1)), false);
					} catch (final IOException e) {
						error(sender, "Could not enable <gold>" + f.getName().substring(1) + "<red>:<reset> " + e.getLocalizedMessage());
						return true;
					}
					f = new File(f.getParentFile(), f.getName().substring(1));
					
					info(sender, "Enabling <gold>" + f.getName() + "<red>...");
					ScriptLoader.loadScripts(new File[] {f});
					if (r.numErrors() == 0) {
						info(sender, "Successfully enabled & parsed <gold>" + f.getName() + "<reset>.");
					} else {
						Skript.error(sender, "Encountered " + r.numErrors() + " error" + (r.numErrors() == 1 ? "" : "s") + " while parsing <gold>" + f.getName() + "<red>!");
					}
					return true;
				}
			} else if (args[0].equalsIgnoreCase("disable")) {
				if (args[1].equals("all")) {
					disableScripts();
					try {
						toggleScripts(false);
						info(sender, "Successfully disabled all scripts!");
					} catch (final IOException e) {
						error(sender, "Could not rename all scripts - some scripts will be enabled again when you restart the server:<reset> " + e.getLocalizedMessage());
					}
				} else {
					final File f = getScriptFromArgs(sender, args, 1);
					if (f == null)
						return true;
					if (f.getName().startsWith("-")) {
						info(sender, "<gold>" + f.getName().substring(1) + "<reset> is already disabled!");
						return true;
					}
					
					ScriptLoader.unloadScript(f);
					
					try {
						FileUtils.move(f, new File(f.getParentFile(), "-" + f.getName()), false);
					} catch (final IOException e) {
						error(sender, "Could not rename <gold>" + f.getName() + "<red>, it will be enabled again when you restart the server:<reset> " + e.getLocalizedMessage());
						return true;
					}
					info(sender, "Successfully disabled <gold>" + f.getName() + "<reset>!");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("update")) {
				Updater.stateLock.writeLock().lock();
				try {
					final UpdateState state = Updater.state;
					if (args[1].equals("check")) {
						switch (state) {
							case NOT_STARTED:
								Updater.check(sender, false, false);
								break;
							case CHECK_IN_PROGRESS:
								info(sender, "" + Updater.m_check_in_progress);
								break;
							case CHECK_ERROR:
								Updater.check(sender, false, false);
								break;
							case CHECKED_FOR_UPDATE:
								if (Updater.latest.get() == null)
									info(sender, Skript.getVersion().isStable() ? "" + Updater.m_running_latest_version : "" + Updater.m_running_latest_version_beta);
								else
									info(sender, "" + Updater.m_update_available);
								break;
							case DOWNLOAD_IN_PROGRESS:
								info(sender, "" + Updater.m_download_in_progress);
								break;
							case DOWNLOAD_ERROR:
								info(sender, "" + Updater.m_download_error);
								break;
							case DOWNLOADED:
								info(sender, "" + Updater.m_downloaded);
								break;
						}
					} else if (args[1].equalsIgnoreCase("changes")) {
						if (state == UpdateState.NOT_STARTED) {
							info(sender, "" + Updater.m_not_started);
						} else if (state == UpdateState.CHECK_IN_PROGRESS) {
							info(sender, "" + Updater.m_check_in_progress);
						} else if (state == UpdateState.CHECK_ERROR) {
							info(sender, "" + Updater.m_check_error);
						} else if (Updater.latest.get() == null) {
							info(sender, Skript.getVersion().isStable() ? "" + Updater.m_running_latest_version : "" + Updater.m_running_latest_version_beta);
						} else if (args.length == 2 && Updater.infos.size() != 1) {
							info(sender, "There have been " + Updater.infos.size() + " updates since your version:");
							String versions = Updater.infos.get(0).version.toString();
							for (int i = Updater.infos.size() - 1; i >= 0; i--)
								versions += ", " + Updater.infos.get(i).version.toString();
							message(sender, "  " + versions);
							message(sender, "To show the changelog of a version type <gold>/skript update changes <version><reset>");
						} else {
							VersionInfo info = null;
							int pageNum = 1;
							if (Updater.infos.size() == 1) {
								info = Updater.latest.get();
								if (args.length >= 3 && args[2].matches("\\d+"))
									pageNum = Utils.parseInt(args[2]);
							} else {
								final String version = args[2];
								for (final VersionInfo i : Updater.infos) {
									if (i.version.toString().equals(version)) {
										info = i;
										break;
									}
								}
								if (info == null) {
									error(sender, "No changelog for the version <gold>" + version + "<red> available");
									return true;
								}
								if (args.length >= 4 && args[3].matches("\\d+"))
									pageNum = Utils.parseInt(args[3]);
							}
							final ChatPage page = ChatPaginator.paginate(info.changelog, pageNum, ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, ChatPaginator.CLOSED_CHAT_PAGE_HEIGHT - 2);
							info(sender, "<bold><cyan>" + info.version + "<reset> (" + info.date + ") <grey>[page " + pageNum + " of " + page.getTotalPages() + "]");
							sender.sendMessage(page.getLines());
							if (pageNum < page.getTotalPages())
								message(sender, "<gray>type <gold>/skript update changes " + (Updater.infos.size() == 1 ? "" : info.version + " ") + (pageNum + 1) + "<gray> for the next page (hint: use the up arrow key)");
						}
					} else if (args[1].equalsIgnoreCase("download")) {
						switch (state) {
							case NOT_STARTED:
								Updater.check(sender, true, false);
								break;
							case CHECK_IN_PROGRESS:
								info(sender, "" + Updater.m_check_in_progress);
								break;
							case CHECK_ERROR:
								Updater.check(sender, true, false);
//						info(sender, Language.format("updater.check_error", updater.error));
								break;
							case CHECKED_FOR_UPDATE:
								if (Updater.latest.get() == null) {
									info(sender, Skript.getVersion().isStable() ? "" + Updater.m_running_latest_version : "" + Updater.m_running_latest_version_beta);
								} else {
									Updater.download(sender, false);
								}
								break;
							case DOWNLOAD_IN_PROGRESS:
								info(sender, "" + Updater.m_download_in_progress);
								break;
							case DOWNLOADED:
								info(sender, "" + Updater.m_downloaded);
								break;
							case DOWNLOAD_ERROR:
								info(sender, "" + Updater.m_download_error);
								break;
						}
					}
				} finally {
					Updater.stateLock.writeLock().unlock();
				}
			} else if (args[0].equalsIgnoreCase("help")) {
				skriptCommandHelp.showHelp(sender);
			}
		} finally {
			r.stop();
		}
		return true;
	}
	
	private static File getScriptFromArgs(final CommandSender sender, final String[] args, final int start) {
		String script = StringUtils.join(args, " ", start, args.length);
		if (!script.endsWith(".sk"))
			script = script + ".sk";
		if (script.startsWith("-"))
			script = script.substring(1);
		File f = new File(Skript.getInstance().getDataFolder(), SCRIPTSFOLDER + File.separator + script);
		if (!f.exists()) {
			f = new File(Skript.getInstance().getDataFolder(), SCRIPTSFOLDER + File.separator + "-" + script);
			if (!f.exists()) {
				error(sender, "Can't find the script <grey>'<gold>" + script + "<grey>'<red> in the scripts folder!");
				return null;
			}
		}
		return f;
	}
	
	private final static Collection<File> toggleScripts(final boolean enable) throws IOException {
		return FileUtils.renameAll(new File(Skript.getInstance().getDataFolder(), SCRIPTSFOLDER), new Converter<String, String>() {
			@Override
			public String convert(final String name) {
				if (name.startsWith("-") == enable)
					return enable ? name.substring(1) : "-" + name;
				return null;
			}
		});
	}
	
}
