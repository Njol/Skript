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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.SingleItemIterator;
import ch.njol.yggdrasil.YggdrasilSerializable;

/**
 * @author Peter Güttinger
 */
public final class VisualEffect implements SyntaxElement, YggdrasilSerializable {
	private final static String LANGUAGE_NODE = "visual effects";
	
	private static enum Type implements YggdrasilSerializable {
		@SuppressWarnings("null")
		ENDER_SIGNAL(Effect.ENDER_SIGNAL),
		@SuppressWarnings("null")
		MOBSPAWNER_FLAMES(Effect.MOBSPAWNER_FLAMES),
		@SuppressWarnings("null")
		POTION_BREAK(Effect.POTION_BREAK) {
			@Override
			public Object getData(final @Nullable Object raw, final Location l) {
				return new PotionEffect(raw == null ? PotionEffectType.SPEED : (PotionEffectType) raw, 1, 0);
			}
		},
		@SuppressWarnings("null")
		SMOKE(Effect.SMOKE) {
			@Override
			public Object getData(final @Nullable Object raw, final Location l) {
				if (raw == null)
					return BlockFace.SELF;
				return Direction.getFacing(((Direction) raw).getDirection(l), false); // TODO allow this to not be a literal
			}
		},
		@SuppressWarnings("null")
		HURT(EntityEffect.HURT),
		@SuppressWarnings("null")
		SHEEP_EAT(EntityEffect.SHEEP_EAT),
		@SuppressWarnings("null")
		WOLF_HEARTS(EntityEffect.WOLF_HEARTS),
		@SuppressWarnings("null")
		WOLF_SHAKE(EntityEffect.WOLF_SHAKE),
		@SuppressWarnings("null")
		WOLF_SMOKE(EntityEffect.WOLF_SMOKE);
		
		final Object effect;
		
		private Type(final Object effect) {
			assert effect instanceof Effect || effect instanceof EntityEffect;
			this.effect = effect;
		}
		
		/**
		 * Converts the data from the pattern to the data required by Bukkit
		 */
		@Nullable
		public Object getData(final @Nullable Object raw, final Location l) {
			assert raw == null;
			return null;
		}
	}
	
	private final static String TYPE_ID = "VisualEffect.Type";
	static {
		Variables.yggdrasil.registerSingleClass(Type.class, TYPE_ID);
		Variables.yggdrasil.registerSingleClass(Effect.class, "Bukkit_Effect");
		Variables.yggdrasil.registerSingleClass(EntityEffect.class, "Bukkit_EntityEffect");
	}
	
	@Nullable
	static SyntaxElementInfo<VisualEffect> info;
	final static List<Type> types = new ArrayList<Type>(Type.values().length);
	final static Noun[] names = new Noun[Type.values().length];
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				final Type[] ts = Type.values();
				types.clear();
				final List<String> patterns = new ArrayList<String>(ts.length);
				for (int i = 0; i < ts.length; i++) {
					final String node = LANGUAGE_NODE + "." + ts[i].name();
					final String pattern = Language.get_(node + ".pattern");
					if (pattern == null) {
						if (Skript.testing())
							Skript.warning("Missing pattern at '" + (node + ".pattern") + "' in the " + Language.getName() + " language file");
					} else {
						types.add(ts[i]);
						patterns.add(pattern);
					}
					if (names[i] == null)
						names[i] = new Noun(node + ".name");
				}
				final String[] ps = patterns.toArray(new String[patterns.size()]);
				assert ps != null;
				info = new SyntaxElementInfo<VisualEffect>(ps, VisualEffect.class);
			}
		});
	}
	
	private Type type;
	@Nullable
	private Object data;
	
	/**
	 * For parsing & deserialisation
	 */
	@SuppressWarnings("null")
	public VisualEffect() {}
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = types.get(matchedPattern);
		assert exprs.length <= 1;
		data = exprs.length == 0 || exprs[0] == null ? null : exprs[0].getSingle(null);
		return true;
	}
	
	public boolean isEntityEffect() {
		return type.effect instanceof EntityEffect;
	}
	
	@Nullable
	public final static VisualEffect parse(final String s) {
		final SyntaxElementInfo<VisualEffect> info = VisualEffect.info;
		if (info == null)
			return null;
		return SkriptParser.parseStatic(Noun.stripIndefiniteArticle(s), new SingleItemIterator<SyntaxElementInfo<VisualEffect>>(info), null);
	}
	
	public void play(final @Nullable Player[] ps, final Location l, final @Nullable Entity e) {
		assert e == null || l.equals(e.getLocation());
		if (isEntityEffect()) {
			if (e != null)
				e.playEffect((EntityEffect) type.effect);
		} else {
			if (ps == null) {
				l.getWorld().playEffect(l, (Effect) type.effect, type.getData(data, l));
			} else {
				for (final Player p : ps)
					p.playEffect(l, (Effect) type.effect, type.getData(data, l));
			}
		}
	}
	
	@Override
	public String toString() {
		return toString(0);
	}
	
	public String toString(final int flags) {
		return names[type.ordinal()].toString(flags);
	}
	
	public static String getAllNames() {
		return StringUtils.join(names, ", ");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + type.hashCode();
		final Object d = data;
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VisualEffect))
			return false;
		final VisualEffect other = (VisualEffect) obj;
		if (type != other.type)
			return false;
		final Object d = data;
		if (d == null) {
			if (other.data != null)
				return false;
		} else if (!d.equals(other.data)) {
			return false;
		}
		return true;
	}
	
}
