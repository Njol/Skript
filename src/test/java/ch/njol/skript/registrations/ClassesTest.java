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

package ch.njol.skript.registrations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.block.Biome;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.data.BukkitClasses;
import ch.njol.skript.classes.data.BukkitEventValues;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.entity.CreeperData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.HorseData;
import ch.njol.skript.entity.SimpleEntityData;
import ch.njol.skript.entity.WolfData;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WeatherType;

/**
 * @author Peter Güttinger
 */
public class ClassesTest {
	
	@Before
	public void before() throws Exception {
		final File f = new File("target/classes/");
		
		final Logger l = Logger.getLogger(getClass().getCanonicalName());
		l.setParent(SkriptLogger.LOGGER);
		l.setLevel(Level.WARNING);
		
		final Server s = EasyMock.createMock(Server.class);
		s.getLogger();
		EasyMock.expectLastCall().andReturn(l).anyTimes();
		s.isPrimaryThread();
		EasyMock.expectLastCall().andReturn(true).anyTimes();
		s.getName();
		EasyMock.expectLastCall().andReturn("Whatever").anyTimes();
		s.getVersion();
		EasyMock.expectLastCall().andReturn("2.0").anyTimes();
		s.getBukkitVersion();
		EasyMock.expectLastCall().andReturn("2.0").anyTimes();
		EasyMock.replay(s);
		
		Bukkit.setServer(s);
		
		@SuppressWarnings("resource")
		final JavaPlugin p = new JavaPlugin() {
			{
				final PluginDescriptionFile pdf = new PluginDescriptionFile(new FileInputStream(new File(f, "plugin.yml")));
				initialize(null, s, pdf, f, new File("target/", "skript.jar"), getClass().getClassLoader());
			}
			
			@Override
			public InputStream getResource(final String filename) {
				try {
					return new FileInputStream(new File(f, filename));
				} catch (final FileNotFoundException e) {
					return null;
				}
			}
		};
		final SkriptAddon a = Skript.registerAddon(p);
		a.setLanguageFileDirectory("lang");
		
		a.loadClasses("ch.njol.skript", "entity");
		new JavaClasses();
		new BukkitClasses();
		new BukkitEventValues();
		new SkriptClasses();
		
		final Field r = Skript.class.getDeclaredField("acceptRegistrations");
		r.setAccessible(true);
		r.set(null, false);
		Classes.onRegistrationsStop();
	}
	
	@Test
	public void test() {
		final Object[] random = {
				// Java
				(byte) 127, (short) 2000, -1600000, 1L << 40, -1.5f, 13.37,
				"String",
				
				// Skript
				Color.BLACK, StructureType.RED_MUSHROOM, WeatherType.THUNDER,
				new Date(System.currentTimeMillis()), new Timespan(1337), new Time(12000), new Timeperiod(1000, 23000),
				new Experience(15), new Direction(0, Math.PI, 10), new Direction(new double[] {0, 1, 0}),
				new EntityType(new SimpleEntityData(HumanEntity.class), 300), new CreeperData(), new SimpleEntityData(Snowball.class), new HorseData(Variant.SKELETON_HORSE), new WolfData(), new XpOrbData(50),
				
				// Bukkit - simple classes only
				GameMode.ADVENTURE, Biome.EXTREME_HILLS, DamageCause.FALL,
				
				// there is also at least one variable for each class on my test server which are tested whenever the server shuts down.
		};
		
		for (final Object o : random) {
			Classes.serialize(o); // includes a deserialisation test
		}
	}
	
}
