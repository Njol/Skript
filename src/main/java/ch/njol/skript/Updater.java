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

package ch.njol.skript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import ch.njol.skript.localization.FormattedMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Version;
import ch.njol.util.ExceptionUtils;
import ch.njol.util.SynchronizedReference;

/**
 * @author Peter Güttinger
 */
public final class Updater {
	
	public final static class VersionInfo {
		Version version;
		String pageURL;
		String changelog;
		Date date;
	}
	
	private final static String rssURL = "http://dev.bukkit.org/server-mods/skript/files.rss";
	
	private final static DateFormat RFC2822 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	
	public static enum UpdateState {
		NOT_STARTED, CHECK_IN_PROGRESS, CHECK_ERROR, CHECKED_FOR_UPDATE, DOWNLOAD_IN_PROGRESS, DOWNLOAD_ERROR, DOWNLOADED;
	}
	
	public final static Object stateLock = new Object();
	/**
	 * must be synchronized with {@link #stateLock}
	 */
	public static volatile UpdateState state = UpdateState.NOT_STARTED;
	private final static SynchronizedReference<String> error = new SynchronizedReference<String>();
	
	public final static List<VersionInfo> infos = new ArrayList<VersionInfo>();
	public final static SynchronizedReference<VersionInfo> latest = new SynchronizedReference<VersionInfo>();
	
	// must be down here as they reference 'error' and 'latest' which are defined above
	public final static Message m_not_started = new Message("updater.not_started");
	public final static Message m_checking = new Message("updater.checking");
	public final static Message m_check_in_progress = new Message("updater.check_in_progress");
	public final static FormattedMessage m_check_error = new FormattedMessage("updater.check_error", error);
	public final static Message m_running_latest_version = new Message("updater.running_latest_version");
	public final static FormattedMessage m_update_available = new FormattedMessage("updater.update_available", latest, Skript.getVersion());
	public final static FormattedMessage m_downloading = new FormattedMessage("updater.downloading", latest);
	public final static Message m_download_in_progress = new Message("updater.download_in_progress");
	public final static FormattedMessage m_download_error = new FormattedMessage("updater.download_error", error);
	public final static Message m_downloaded = new Message("updater.downloaded");
	
	/**
	 * Must only be called if {@link #state} == {@link UpdateState#NOT_STARTED} or {@link UpdateState#CHECK_ERROR}
	 * 
	 * @param sender Sender to recieve messages or null if this is an automatic check
	 * @param download
	 */
	static void check(final CommandSender sender, final boolean download, final boolean isAutomatic) {
		assert sender != null;
		synchronized (stateLock) {
			if (state != UpdateState.NOT_STARTED && state != UpdateState.CHECK_ERROR)
				throw new IllegalStateException("Cannot check for an update twice");
			state = UpdateState.CHECK_IN_PROGRESS;
		}
		Skript.info(sender, Language.get("updater.checking"));
		new Thread(new Runnable() {
			@SuppressWarnings("resource")
			@Override
			public void run() {
				InputStream in = null;
				InputStreamReader r = null;
				try {
					final URLConnection conn = new URL(rssURL).openConnection();
					in = conn.getInputStream();
					r = new InputStreamReader(in, conn.getContentEncoding() == null ? "UTF-8" : conn.getContentEncoding());
					final XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(r);
					
					infos.clear();
					VersionInfo current = null;
					
					while (reader.hasNext()) {
						XMLEvent e = reader.nextEvent();
						if (e.isStartElement()) {
							final String element = e.asStartElement().getName().getLocalPart();
							if (current == null) {
								if (element.equalsIgnoreCase("item")) {
									current = new VersionInfo();
								}
							} else {
								if (element.equalsIgnoreCase("title")) {
									final String version = reader.nextEvent().asCharacters().getData();
									if (!version.matches("\\d+\\.\\d+(\\.\\d+)? \\(zip\\)")) {// not the default version pattern to not match beta/etc. versions
										current = null;
										continue;
									}
									current.version = new Version(version.substring(0, version.length() - " (zip)".length()));
									if (current.version.compareTo(Skript.getVersion()) <= 0)
										break;
								} else if (element.equalsIgnoreCase("link")) {
									current.pageURL = reader.nextEvent().asCharacters().getData();
								} else if (element.equalsIgnoreCase("description")) {
									String cl = "";
									while ((e = reader.nextEvent()).isCharacters())
										cl += e.asCharacters().getData();
									current.changelog = "- " + StringEscapeUtils.unescapeHtml(cl).replace("<br>", "").replace("<p>", "").replace("</p>", "").replaceAll("\n(?!\n)", "\n- ");
								} else if (element.equalsIgnoreCase("pubDate")) {
									current.date = new Date(RFC2822.parse(reader.nextEvent().asCharacters().getData()).getTime());
								}
							}
						} else if (e.isEndElement()) {
							if (e.asEndElement().getName().getLocalPart().equalsIgnoreCase("item")) {
								if (current != null)
									infos.add(current);
								current = null;
							}
						}
					}
					
					if (!infos.isEmpty()) {
						Collections.sort(infos, new Comparator<VersionInfo>() {
							@Override
							public int compare(final VersionInfo v1, final VersionInfo v2) {
								return v2.version.compareTo(v1.version);
							}
						});
						latest.set(infos.get(0));
					}
					
					final String message = infos.isEmpty() ? "" + m_running_latest_version : "" + m_update_available;
					if (isAutomatic && !infos.isEmpty()) {
						Skript.adminBroadcast(message);
					} else {
						Skript.info(sender, message);
					}
					
					if (download && !infos.isEmpty()) {
						synchronized (stateLock) {
							state = UpdateState.DOWNLOAD_IN_PROGRESS;
						}
						download_i(sender, isAutomatic);
					} else {
						synchronized (stateLock) {
							state = UpdateState.CHECKED_FOR_UPDATE;
						}
					}
				} catch (final IOException e) {
					Skript.error(sender, Language.format("updater.check_error", ExceptionUtils.toString(e)));
					synchronized (stateLock) {
						state = UpdateState.CHECK_ERROR;
						error.set(ExceptionUtils.toString(e));
					}
				} catch (final Exception e) {
					Skript.error(sender, "An internal error occurred while checking for the latest version of Skript. See the erver log for details.");
					Skript.exception(e, "Error while checking for a new version of Skript");
					synchronized (stateLock) {
						state = UpdateState.CHECK_ERROR;
						error.set(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
					}
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (final IOException e) {}
					}
					if (r != null) {
						try {
							r.close();
						} catch (final IOException e) {}
					}
				}
			}
		}).start();
	}
	
	private final static String getFileURL(final String pageURL) throws MalformedURLException, IOException {
		InputStream in = null;
		try {
			final URLConnection conn = new URL(pageURL).openConnection();
			in = conn.getInputStream();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in, conn.getContentEncoding() == null ? "UTF-8" : conn.getContentEncoding()));
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					// the line with the download link looks like this:
					// #whitespace#<li class="user-action user-action-download"><span><a href="http://dev.bukkit.org/media/files/#number#/#number#/#name#.#ext#">Download</a></span></li>
					final int s = line.indexOf("<a href=\"http://dev.bukkit.org/media/files/");
					if (s == -1)
						continue;
					final int e = line.indexOf("\">", s + "<a href=\"http://dev.bukkit.org/media/files/".length());
					if (e == -1)
						continue;
					return line.substring(s + "<a href=\"".length(), e);
				}
			} finally {
				reader.close();
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {}
			}
		}
		throw new IOException("Could not get the file's URL. You can however download Skript manually from " + pageURL);
	}
	
	/**
	 * Must set {@link #state} to {@link UpdateState#DOWNLOAD_IN_PROGRESS} prior to calling this
	 * 
	 * @param sender not null
	 */
	private static void download_i(final CommandSender sender, final boolean isAutomatic) {
		synchronized (stateLock) {
			if (state != UpdateState.DOWNLOAD_IN_PROGRESS)
				throw new IllegalStateException();
		}
		Skript.info(sender, "" + m_downloading);
		boolean hasJar = false;
		final boolean hasConfig = true;
		ZipInputStream zip = null;
		try {
			final URLConnection conn = new URL(getFileURL(latest.get().pageURL)).openConnection();
			zip = new ZipInputStream(conn.getInputStream());
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.getName().endsWith("Skript.jar")) {
					Bukkit.getUpdateFolderFile().mkdirs();
					final File update = new File(Bukkit.getUpdateFolderFile(), "Skript.jar");
					FileOutputStream out = null;
					try {
						out = new FileOutputStream(update);
						final byte[] buffer = new byte[4 * 1024];
						int read;
						while ((read = zip.read(buffer)) > 0) {
							out.write(buffer, 0, read);
						}
					} finally {
						if (out != null)
							out.close();
					}
					hasJar = true;
//				} else if (entry.getName().endsWith("config.sk")) {
					// TODO automagically update new config options
					// important: only update config if the jar was successfully extracted!
					//hasConfig = true;
				}
				if (hasJar && hasConfig)
					break;
				zip.closeEntry();
			}
			if (isAutomatic)
				Skript.adminBroadcast(Language.get("updater.downloaded"));
			else
				Skript.info(sender, Language.get("updater.downloaded"));
			synchronized (stateLock) {
				state = UpdateState.DOWNLOADED;
			}
		} catch (final IOException e) {
			Skript.error(sender, Language.format("updater.download_error", ExceptionUtils.toString(e)));
			synchronized (stateLock) {
				state = UpdateState.DOWNLOAD_ERROR;
				error.set(ExceptionUtils.toString(e));
			}
		} catch (final Exception e) {
			Skript.exception(e, "Error while downloading the latest version of Skript");
			synchronized (stateLock) {
				state = UpdateState.DOWNLOAD_ERROR;
				error.set(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
			}
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (final IOException e) {}
			}
		}
	}
	
	/**
	 * Must only be called if {@link #state} == {@link UpdateState#CHECKED_FOR_UPDATE} or {@link UpdateState#DOWNLOAD_ERROR}
	 * 
	 * @param sender
	 */
	public static void download(final CommandSender sender, final boolean isAutomatic) {
		assert sender != null;
		synchronized (stateLock) {
			if (state != UpdateState.CHECKED_FOR_UPDATE && state != UpdateState.DOWNLOAD_ERROR)
				throw new IllegalStateException("Must check for an update first");
			state = UpdateState.DOWNLOAD_IN_PROGRESS;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				download_i(sender, isAutomatic);
			}
		}).start();
	}
	
}
