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

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.util.Kleenean;
import ch.njol.util.iterator.SingleItemIterator;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public final class VisualEffect implements SyntaxElement {
	private final static String LANGUAGE_NODE = "effects";
	
	private static enum Type {
		ENDER_SIGNAL(Effect.ENDER_SIGNAL),
		MOBSPAWNER_FLAMES(Effect.MOBSPAWNER_FLAMES),
		POTION_BREAK(Effect.POTION_BREAK),
		SMOKE(Effect.SMOKE),
		HURT(EntityEffect.HURT),
		SHEEP_EAT(EntityEffect.SHEEP_EAT),
		WOLF_HEARTS(EntityEffect.WOLF_HEARTS),
		WOLF_SHAKE(EntityEffect.WOLF_SHAKE),
		WOLF_SMOKE(EntityEffect.WOLF_SMOKE);
		
		final Object effect;
		
		private Type(final Object effect) {
			assert effect instanceof Effect || effect instanceof EntityEffect;
			this.effect = effect;
		}
		
	}
	
	static SyntaxElementInfo<VisualEffect> info;
	static {
		final Type[] ts = Type.values();
		final String[] patterns = new String[ts.length];
		for (int i = 0; i < ts.length; i++) {
//			patterns[i] = Language.get_(LANGUAGE_NODE + "." + ts[i].name() + ".pattern");
		}
		info = new SyntaxElementInfo<VisualEffect>(patterns, VisualEffect.class);
	}
	
	private Type type;
	private Object data;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		
		return true;
	}
	
	public boolean isEntityEffect() {
		return type.effect instanceof EntityEffect;
	}
	
	public final static VisualEffect parse(final String s) {
		final VisualEffect e = SkriptParser.parse(s, new SingleItemIterator<SyntaxElementInfo<VisualEffect>>(info), null);
		//TODO
		return e;
	}
	
	public void play(final Player p, final Location l, final Entity e) {
		if (isEntityEffect()) {
			e.playEffect((EntityEffect) type.effect);
		} else {
			p.playEffect(l, (Effect) type.effect, data);
		}
	}
	
}
