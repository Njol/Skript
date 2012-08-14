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

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;
import static org.easymock.EasyMock.*;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.Test;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.ConfigReader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Trigger;

/**
 * @author Peter Güttinger
 * 
 */
public class SkriptTest {
	
	private static Player njol = createMock(Player.class);
	static {
		
	}

//	@Test
	public static void main() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				org.bukkit.craftbukkit.Main.main(new String[] {"-nojline"});
			}
		}).start();
		while (Bukkit.getServer() == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
			@Override
			public void run() {
				assertNotNull(Skript.getInstance());
				test();
			}
		}, 2);
	}
	
	private final static void test() {
		
		Trigger t = ScriptLoader.loadTrigger(nodeFromString("on rightclick on air:\n kill player"));
		t.run(new PlayerInteractEvent(njol, Action.LEFT_CLICK_AIR, null, null, null));
		
		
	}
	
	private final static SectionNode nodeFromString(String s) {
		try {
			return (SectionNode) new Config(s, "test.sk", true, ":").getMainNode().getNodeList().get(0);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
