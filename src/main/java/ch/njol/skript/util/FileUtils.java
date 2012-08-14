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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

/**
 * @author Peter Güttinger
 * 
 */
public abstract class FileUtils {
	
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
		Files.copy(f.toPath(), backup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
		return backup;
	}
	
}
