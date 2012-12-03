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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Version;
import ch.njol.util.iterator.EnumerationIterable;

/**
 * Utility class for Skript addons.
 * 
 * @author Peter Güttinger
 */
public class SkriptAddon {
	
	public final JavaPlugin plugin;
	public final Version version;
	public final String name;
	
	/**
	 * Protected constructor. Use {@link Skript#registerAddon(JavaPlugin)} to get a SkriptAddon for your plugin.
	 * 
	 * @param p
	 */
	SkriptAddon(final JavaPlugin p) {
		plugin = p;
		name = p.getName();
		Version v;
		try {
			v = new Version(p.getDescription().getVersion());
		} catch (final IllegalArgumentException e) {
			final Matcher m = Pattern.compile("\\d+(\\.\\d+(\\.\\d+)?)?").matcher(p.getDescription().getVersion());
			if (!m.find())
				throw new IllegalArgumentException("The version of the plugin " + p.getName() + " does not contain any numbers: " + p.getDescription().getVersion());
			Skript.warning("The plugin " + p.getName() + " uses an invalid version syntax");
			v = new Version(m.group());
		}
		version = v;
	}
	
	/**
	 * Loads classes of the plugin by package. Useful for registering many syntax elements like Skript does it.
	 * 
	 * @param plugin Plugin to load the classes from
	 * @param basePackage The base package to add to all sub packages, e.g. <tt>"ch.njol.skript"</tt>.
	 * @param subPackages Subpackages of the base package, e.g. <tt>"expressions", "conditions", "effects"</tt> (Note: subpackages of these packages will not be loaded
	 *            automatically)
	 * @throws IOException If some error occurred attempting to read the plugin's jar file.
	 * @return This SkriptAddon
	 */
	public SkriptAddon loadClasses(String basePackage, final String... subPackages) throws IOException {
		assert subPackages.length > 0;
		final JarFile jar = new JarFile(getFile());
		for (int i = 0; i < subPackages.length; i++)
			subPackages[i] = subPackages[i].replace('.', '/') + "/";
		basePackage = basePackage.replace('.', '/') + "/";
		try {
			entryLoop: for (final JarEntry e : new EnumerationIterable<JarEntry>(jar.entries())) {
				if (e.getName().startsWith(basePackage) && e.getName().endsWith(".class")) {
					for (final String sub : subPackages) {
						if (e.getName().startsWith(sub, basePackage.length()) && e.getName().lastIndexOf('/') == basePackage.length() + sub.length() - 1) {
							final String c = e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length());
							try {
								Class.forName(c, true, plugin.getClass().getClassLoader());
							} catch (final ClassNotFoundException ex) {
								Skript.exception(ex, "Cannot load class " + c);
							} catch (final ExceptionInInitializerError err) {
								Skript.exception(err.getCause(), "Class " + c + " generated an exception while loading");
							}
							continue entryLoop;
						}
					}
				}
			}
		} finally {
			try {
				jar.close();
			} catch (final IOException e) {}
		}
		return this;
	}
	
	private String languageFileDirectory = null;
	
	/**
	 * The directory where language files are located, e.g. "lang" or "skript lang" if you have a lang folder yourself. Localized files will be read from the plugin's jar and the
	 * plugin's data folder, but the default english file with all default strings is only taken from the jar and <b>must</b> exist!
	 * 
	 * @param directory Directory name without ending slash
	 * @return This SkriptAddon
	 */
	public SkriptAddon setLanguageFileDirectory(String directory) {
		assert directory != null;
		if (languageFileDirectory != null)
			throw new IllegalStateException();
		if (directory.endsWith("/"))
			directory = directory.substring(0, directory.length() - 1);
		languageFileDirectory = directory;
		Language.loadDefault(this);
		return this;
	}
	
	public String getLanguageFileDirectory() {
		return languageFileDirectory;
	}
	
	private File file = null;
	
	/**
	 * @return The jar file of the plugin. The first invocation of this emthod uses reflection to invoke the protected method {@link JavaPlugin#getFile()} to get the plugin's jar
	 *         file. The file is then cached and returned upon subsequent calls to this method to reduce usage of reflection.
	 */
	public File getFile() {
		if (file != null)
			return file;
		try {
			final Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
			getFile.setAccessible(true);
			file = (File) getFile.invoke(plugin);
			return file;
		} catch (final NoSuchMethodException e) {
			Skript.outdatedError(e);
		} catch (final IllegalArgumentException e) {
			Skript.outdatedError(e);
		} catch (final IllegalAccessException e) {
			assert false;
		} catch (final SecurityException e) {
			throw new RuntimeException(e);
		} catch (final InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
}
