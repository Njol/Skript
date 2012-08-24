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

/**
 * @author Peter Güttinger
 * 
 */
abstract class Java7FileUtils {
	
	private Java7FileUtils() {}
	
	public final static void move(final File from, final File to) throws IOException {
		Files.move(from.toPath(), to.toPath());
	}
	
	public final static void copy(final File from, final File to) throws IOException {
		Files.copy(from.toPath(), to.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
	}
	
}
