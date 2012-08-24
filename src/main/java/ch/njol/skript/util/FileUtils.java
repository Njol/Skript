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

package ch.njol.skript.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import ch.njol.skript.classes.Converter;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class FileUtils {
	
	private final static boolean RUNNINGJAVA7 = !System.getProperty("java.version").startsWith("1.6");
	
	private FileUtils() {}
	
	private final static SimpleDateFormat backupFormat = new SimpleDateFormat("yyyy-MM-dd_kk-mm");
	
	public final static File backup(final File f) throws IOException {
		String name = f.getName();
		final int c = name.lastIndexOf('.');
		final String ext = c == -1 ? null : name.substring(c + 1);
		if (c != -1)
			name = name.substring(0, c);
		final File backup = new File(f.getParentFile(), name + "_backup_" + backupFormat.format(System.currentTimeMillis()) + (ext == null ? "" : "." + ext));
		if (backup.exists())
			throw new IOException("backup file " + backup.getName() + " does already exist");
		copy(f, backup);
		return backup;
	}
	
	public final static void move(final File from, final File to) throws IOException {
		if (RUNNINGJAVA7) {
			Java7FileUtils.move(from, to);
		} else {
			if (!from.renameTo(to))
				throw new IOException("Can't rename " + from.getName() + " to " + to.getName());
		}
	}
	
	public final static void copy(final File from, final File to) throws IOException {
		if (RUNNINGJAVA7) {
			Java7FileUtils.copy(from, to);
		} else {
			if (!to.createNewFile())
				throw new IOException("Can't copy " + from.getName() + " to " + to.getName() + ": Can't create new file");
			FileInputStream in = null;
			FileOutputStream out = null;
			try {
				in = new FileInputStream(from);
				out = new FileOutputStream(to);
				final byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = in.read(buffer)) != -1)
					out.write(buffer, 0, bytesRead);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (final IOException e) {}
				}
				if (out != null) {
					try {
						out.close();
					} catch (final IOException e) {}
				}
			}
		}
	}
	
	/**
	 * @param directory
	 * @param renamer Renames files. Return null to leave a file as-is.
	 * @return A collection of all changed files (with their new names)
	 * @throws IOException
	 */
	public final static Collection<File> renameAll(final File directory, final Converter<String, String> renamer) throws IOException {
		final Collection<File> changed = new ArrayList<File>();
		for (final File f : directory.listFiles()) {
			if (f.isDirectory()) {
				changed.addAll(renameAll(f, renamer));
			} else {
				final String newName = renamer.convert(f.getName());
				if (newName == null)
					continue;
				final File newFile = new File(f.getParent(), newName);
				move(f, newFile);
				changed.add(newFile);
			}
		}
		return changed;
	}
	
}
