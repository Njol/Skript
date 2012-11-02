package ch.njol.skript;

import static ch.njol.skript.Skript.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import ch.njol.skript.Updater.UpdateState;
import ch.njol.skript.Updater.VersionInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.log.SimpleLog;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.CommandHelp;
import ch.njol.skript.util.FileUtils;

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
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

/**
 * @author Peter Güttinger
 * 
 */
public class SkriptCommand implements CommandExecutor {
	
	private final static CommandHelp skriptCommandHelp = new CommandHelp("<gray>/<gold>skript", "Skript's main command", "cyan")
			.add(new CommandHelp("reload", "Reloads the config, all scripts, everything, or a specific script", "red")
					.add("all", "Reloads all configs and all scripts")
					.add("config", "Reloads the main config")
					.add("aliases", "Reloads the aliases config")
					.add("scripts", "Reloads all scripts")
					.add("<script>", "Reloads a specific script")
			).add(new CommandHelp("enable", "Enables all scripts or a specific one", "red")
					.add("all", "Enables all scripts")
					.add("<script>", "Enables a specific script")
			).add(new CommandHelp("disable", "Disables all scripts or a specific one", "red")
					.add("all", "Disables all scripts")
					.add("<script>", "Disables a specific script")
			).add(new CommandHelp("update", "Check for updates, read the changelog, and download the latest version of Skript", "red")
					.add("check", "Checks for a new version")
					.add("changes", "See what changed in the newest version")
					.add("download", "Download the newest version")
			//			).add(new CommandHelp("variable", "Commands for modifying variables", "red")
//					.add("set", "Creates a new variable or changes an existing one")
//					.add("delete", "Deletes a variable")
//					.add("find", "Find variables")
			).add("help", "Prints this help message");
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!skriptCommandHelp.test(sender, args))
			return true;
		if (args[0].equalsIgnoreCase("reload")) {
			if (args[1].equalsIgnoreCase("all")) {
				final SimpleLog log = SkriptLogger.startSubLog();
				reload();
				log.stop();
				if (log.hasErrors()) {
					error(sender, "Error(s) while reloading the config and all scripts:");
					log.printErrors(sender, null);
				} else {
					info(sender, "Successfully reloaded the config and all scripts");
				}
				return true;
			} else if (args[1].equalsIgnoreCase("scripts")) {
				final SimpleLog log = SkriptLogger.startSubLog();
				reloadScripts();
				log.stop();
				if (log.hasErrors()) {
					error(sender, "Error(s) while reloading all scripts:");
					log.printErrors(sender, null);
				} else {
					info(sender, "Successfully reloaded all scripts");
				}
			} else if (args[1].equalsIgnoreCase("config")) {
				final SimpleLog log = SkriptLogger.startSubLog();
				reloadMainConfig();
				log.stop();
				if (log.hasErrors()) {
					error(sender, "Error(s) while reloading the main config:");
					log.printErrors(sender, null);
				} else {
					info(sender, "Successfully reloaded the main config");
				}
			} else if (args[1].equalsIgnoreCase("aliases")) {
				final SimpleLog log = SkriptLogger.startSubLog();
				reloadAliases();
				log.stop();
				if (log.hasErrors()) {
					error(sender, "Error(s) while reloading the aliases config:");
					log.printErrors(sender, null);
				} else {
					info(sender, "Successfully reloaded the aliases config");
				}
			} else {
				final File f = getScriptFromArgs(sender, args, 1);
				if (f == null)
					return true;
				if (f.getName().startsWith("-")) {
					info(sender, "This script is currently disabled. Use <gray>/<gold>skript <cyan>enable <red>" + f.getName().substring(1, f.getName().length() - 3) + "<reset> to enable it.");
					return true;
				}
				ScriptLoader.unloadScript(f);
				final SimpleLog log = SkriptLogger.startSubLog();
				ScriptLoader.loadScripts(Arrays.asList(f));
				log.stop();
				if (log.hasErrors()) {
					error(sender, "Error(s) while reloading <gold>" + f.getName() + "<red>:");
					log.printErrors(sender, null);
				} else {
					info(sender, "Successfully reloaded <gold>" + f.getName() + "<reset>!");
				}
				return true;
			}
		} else if (args[0].equalsIgnoreCase("enable")) {
			if (args[1].equals("all")) {
				try {
					final Collection<File> files = toggleScripts(true);
					final SimpleLog log = SkriptLogger.startSubLog();
					ScriptLoader.loadScripts(files);
					log.stop();
					if (log.hasErrors()) {
						error(sender, "Error(s) while loading disabled scripts:");
						log.printErrors(sender, null);
					} else {
						info(sender, "Successfully loaded & enabled all previously disabled scripts!");
					}
				} catch (final IOException e) {
					error(sender, "Could not enable any scripts (some scripts might however have been renamed already): " + e.getLocalizedMessage());
				}
			} else {
				File f = getScriptFromArgs(sender, args, 1);
				if (f == null)
					return true;
				if (!f.getName().startsWith("-")) {
					info(sender, "<gold>" + f.getName() + "<reset> is already enabled!");
					return true;
				}
				
				try {
					FileUtils.move(f, new File(f.getParentFile(), f.getName().substring(1)), false);
				} catch (final IOException e) {
					error(sender, "Could not enable <gold>" + f.getName().substring(1) + "<red>:<reset> " + e.getLocalizedMessage());
					return true;
				}
				f = new File(f.getParentFile(), f.getName().substring(1));
				
				final SimpleLog log = SkriptLogger.startSubLog();
				ScriptLoader.loadScripts(Arrays.asList(f));
				log.stop();
				if (log.hasErrors()) {
					error(sender, "Error(s) while enabling <gold>" + f.getName() + "<red>:");
					log.printErrors(sender, null);
				} else {
					info(sender, "Successfully enabled <gold>" + f.getName() + "<reset>!");
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
			final Updater updater = Updater.getInstance();
			synchronized (updater.stateLock) {
				final UpdateState state = updater.state;
				if (args[1].equals("check")) {
					switch (state) {
						case NOT_STARTED:
							updater.check(sender, false, false);
							break;
						case CHECK_IN_PROGRESS:
							info(sender, Language.get("updater.check_in_progress"));
							break;
						case CHECK_ERROR:
							updater.check(sender, false, false);
							break;
						case CHECKED_FOR_UPDATE:
							if (updater.latest == null)
								info(sender, Language.get("updater.running_latest_version"));
							else
								info(sender, Language.format("updater.update_available", updater.latest.version, Skript.getVersion()));
							break;
						case DOWNLOAD_IN_PROGRESS:
							info(sender, Language.get("updater.download_in_progress"));
							break;
						case DOWNLOAD_ERROR:
							info(sender, Language.format("updater.download_error", updater.error));
							break;
						case DOWNLOADED:
							info(sender, Language.get("updater.downloaded"));
							break;
					}
				} else if (args[1].equalsIgnoreCase("changes")) {
					if (state == UpdateState.NOT_STARTED || state == UpdateState.CHECK_IN_PROGRESS || state == UpdateState.CHECK_ERROR) {
						if (state == UpdateState.CHECK_ERROR)
							info(sender, Language.format("updater.check_error", updater.error));
						else
							info(sender, Language.get("updater." + state.name()));
					} else if (updater.latest == null) {
						info(sender, Language.get("updater.running_latest_version"));
					} else if (args.length == 2 && updater.infos.size() != 1) {
						info(sender, "There have been " + updater.infos.size() + " updates since your version:");
						String versions = updater.infos.get(0).version.toString();
						for (int i = updater.infos.size() - 1; i >= 0; i--)
							versions += ", " + updater.infos.get(i).version.toString();
						message(sender, versions);
						message(sender, "To show the changelog of a version type <gold>/skript update changes <version><reset>");
					} else {
						VersionInfo info = null;
						int pageNum = 1;
						if (updater.infos.size() == 1) {
							info = updater.latest;
							if (args.length >= 3 && args[2].matches("\\d+"))
								pageNum = Skript.parseInt(args[2]);
						} else {
							final String version = args[2];
							for (final VersionInfo i : updater.infos) {
								if (i.version.toString().equals(version)) {
									info = i;
									break;
								}
							}
							if (info == null) {
								error(sender, "No changelog for the version '" + version + "' available");
								return true;
							}
							if (args.length >= 4 && args[3].matches("\\d+"))
								pageNum = Skript.parseInt(args[3]);
						}
						final ChatPage page = ChatPaginator.paginate(info.changelog, pageNum, ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, ChatPaginator.CLOSED_CHAT_PAGE_HEIGHT - 2);
						info(sender, "<bold><cyan>" + info.version + "<reset> (" + info.date + ") <grey>[page " + pageNum + " of " + page.getTotalPages() + "]");
						sender.sendMessage(page.getLines());
						if (pageNum < page.getTotalPages())
							message(sender, "  type <gold>/skript update changes " + (updater.infos.size() == 1 ? "" : info.version + " ") + (pageNum + 1) + "<reset> for the next page");
					}
				} else if (args[1].equalsIgnoreCase("download")) {
					switch (state) {
						case NOT_STARTED:
							updater.check(sender, true, false);
							break;
						case CHECK_IN_PROGRESS:
							info(sender, Language.get("updater.check_in_progress"));
							break;
						case CHECK_ERROR:
							updater.check(sender, true, false);
//						info(sender, Language.format("updater.check_error", updater.error));
							break;
						case CHECKED_FOR_UPDATE:
							if (updater.latest == null) {
								info(sender, Language.get("updater.running_latest_version"));
							} else {
								updater.download(sender, false);
							}
							break;
						case DOWNLOAD_IN_PROGRESS:
							info(sender, Language.get("updater.download_in_progress"));
							break;
						case DOWNLOADED:
							info(sender, Language.get("updater.downloaded"));
							break;
						case DOWNLOAD_ERROR:
							info(sender, Language.format("updater.download_error", updater.error));
							break;
					}
				}
			}
		} else if (args[0].equalsIgnoreCase("help")) {
			skriptCommandHelp.showHelp(sender);
		}
		return true;
	}
	
	private static File getScriptFromArgs(final CommandSender sender, final String[] args, final int start) {
		final StringBuilder b = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			b.append(" " + args[i]);
		String script = b.toString().trim();
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
