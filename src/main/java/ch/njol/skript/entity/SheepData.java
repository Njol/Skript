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

package ch.njol.skript.entity;

import java.util.Arrays;

import org.bukkit.entity.Sheep;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.util.Checker;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class SheepData extends EntityData<Sheep> {
	static {
		EntityData.register(SheepData.class, "sheep", Sheep.class, "unsheared sheep", "sheep", "sheared sheep");
	}
	
	private Color[] colors = null;
	private int sheared = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		sheared = matchedPattern - 1;
		if (exprs[0] != null)
			colors = ((Literal<Color>) exprs[0]).getAll();
		return true;
	}
	
	@Override
	protected boolean init(final Class<? extends Sheep> c, final Sheep e) {
		sheared = e == null ? 0 : e.isSheared() ? 1 : -1;
		colors = e == null ? null : new Color[] {Color.byWoolColor(e.getColor())};
		return true;
	}
	
	@Override
	public void set(final Sheep entity) {
		if (colors != null)
			entity.setColor(CollectionUtils.random(colors).getWoolColor());
	}
	
	@Override
	public boolean match(final Sheep entity) {
		return (sheared == 0 || entity.isSheared() == (sheared == 1))
				&& (colors == null || SimpleExpression.check(colors, new Checker<Color>() {
					@Override
					public boolean check(final Color c) {
						return entity.getColor() == c.getWoolColor();
					}
				}, false, false));
	}
	
	@Override
	public Class<Sheep> getType() {
		return Sheep.class;
	}
	
	private final static ArgsMessage format = new ArgsMessage("entities.sheep.format");
	
	@Override
	public String toString() {
		if (colors == null)
			return super.toString();
		return format.toString(super.toString(), Classes.toString(colors, false));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(colors);
		result = prime * result + sheared;
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SheepData))
			return false;
		final SheepData other = (SheepData) obj;
		if (!Arrays.equals(colors, other.colors))
			return false;
		if (sheared != other.sheared)
			return false;
		return true;
	}
	
	@Override
	public String serialize() {
		if (colors != null) {
			final StringBuilder b = new StringBuilder();
			b.append(sheared);
			b.append("|");
			for (final Color c : colors) {
				if (b.length() != 0)
					b.append(",");
				b.append(c.name());
			}
			return b.toString();
		} else {
			return "" + sheared;
		}
	}
	
	@Override
	protected boolean deserialize(final String s) {
		final String[] split = s.split("\\|");
		final String sh;
		if (split.length == 1) {
			sh = s;
		} else if (split.length == 2) {
			sh = split[0];
			final String[] cs = split[1].split(",");
			colors = new Color[cs.length];
			for (int i = 0; i < cs.length; i++) {
				try {
					colors[i] = Color.valueOf(cs[i]);
				} catch (final IllegalArgumentException e) {
					return false;
				}
			}
		} else {
			return false;
		}
		try {
			sheared = Integer.parseInt(sh);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof SheepData)
			return colors == null || CollectionUtils.isSubset(colors, ((SheepData) e).colors);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new SheepData();
	}
	
}
