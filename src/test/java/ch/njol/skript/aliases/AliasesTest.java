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

package ch.njol.skript.aliases;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Set;

import org.junit.Test;

/**
 * @author Peter Güttinger
 */
public class AliasesTest {
	
	@Test
	public void testNames() {
		final ItemType t = new ItemType();
		
		final Aliases.Variations v = new Aliases.Variations();
		final HashMap<String, ItemType> var1 = new HashMap<String, ItemType>();
		var1.put("{default}", new ItemType());
		var1.put("v1.1", new ItemType());
		var1.put("v1.2", new ItemType());
		v.put("var1", var1);
		final HashMap<String, ItemType> var2 = new HashMap<String, ItemType>();
		var2.put("v2.1 @a", new ItemType());
		var2.put("v2.2", new ItemType());
		v.put("var2", var2);
		final HashMap<String, ItemType> var3 = new HashMap<String, ItemType>();
		var3.put("v3.1¦s", new ItemType());
		var3.put("v3.2¦a¦b", new ItemType());
		v.put("var3", var3);
		
		final String[][] tests = {
				{"a", "a"},
				{"a[b]c", "abc", "ac"},
				{"a [b] c", "a b c", "a c"},
				{"a(b|c)d", "abd", "acd"},
				{"a(b|)c", "abc", "ac"},
				{"a {var1}", "a", "a v1.1", "a v1.2"},
				{"a @an {var2}", "a @an", "a v2.1@a", "a v2.2@an"},
				{"{var3}", "", "v3.1¦¦s¦", "v3.2¦a¦b¦"},
		};
		
		for (final String[] test : tests) {
			final Set<String> names = Aliases.getAliases(test[0], t, v).keySet();
			int i = 1;
			for (final String name : names)
				assertEquals(test[i++], name);
		}
	}
	
}
