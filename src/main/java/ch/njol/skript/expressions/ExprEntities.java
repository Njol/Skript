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

package ch.njol.skript.expressions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.SimpleLog;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Checker;
import ch.njol.util.iterator.CheckedIterator;
import ch.njol.util.iterator.NonNullIterator;

/**
 * @author Peter Güttinger
 */
public class ExprEntities extends SimpleExpression<Entity> {
	private static final long serialVersionUID = 2659125624066080969L;
	
	static {
		Skript.registerExpression(ExprEntities.class, Entity.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
				"[all] <.+> [(in|of) [world[s]] %-worlds%]",
				"[all] entities of type[s] %entitydatas% [(in|of) [world[s]] %-worlds%]",
				"[all] <.+> in radius %double% (of|around) %location%",
				"[all] entities of type[s] %entitydatas% in radius %double% (of|around) %location%");
	}
	
	private Expression<? extends EntityData<?>> types;
	
	private Expression<World> worlds;
	
	private Expression<Double> radius;
	private Expression<Location> center;
	private Expression<? extends Entity> centerEntity;
	
	private Class<? extends Entity> returnType = Entity.class;
	
	private int matchedPattern;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		this.matchedPattern = matchedPattern;
		if (!parseResult.regexes.isEmpty()) {
			final SimpleLog log = SkriptLogger.startSubLog();
			final EntityData<?> d = EntityData.parseWithoutAnOrAny(parseResult.regexes.get(0).group());
			log.stop();
			if (d == null || !d.isPlural())
				return false;
			types = new SimpleLiteral<EntityData<?>>(d, false);
			log.printLog();
		} else {
			types = (Expression<? extends EntityData<?>>) exprs[0];
		}
		if (matchedPattern < 2) {
			worlds = (Expression<World>) exprs[exprs.length - 1];
		} else {
			radius = (Expression<Double>) exprs[exprs.length - 2];
			center = (Expression<Location>) exprs[exprs.length - 1];
			final SimpleLog log = SkriptLogger.startSubLog();
			centerEntity = center.getSource().getConvertedExpression(Entity.class);
			log.stop();
		}
		if (types instanceof Literal && ((Literal<EntityData<?>>) types).getAll().length == 1) {
			returnType = ((Literal<EntityData<?>>) types).getSingle().getType();
		}
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return returnType;
	}
	
	@Override
	protected Entity[] get(final Event e) {
		if (matchedPattern >= 2) {
			final List<Entity> l = new ArrayList<Entity>();
			final Iterator<? extends Entity> iter = iterator(e);
			while (iter.hasNext())
				l.add(iter.next());
			return l.toArray((Entity[]) Array.newInstance(returnType, l.size()));
		} else {
			return EntityData.getAll(types.getAll(e), returnType, worlds == null ? null : worlds.getArray(e));
		}
	}
	
	@Override
	public Iterator<? extends Entity> iterator(final Event e) {
		if (matchedPattern >= 2) {
			final Entity en;
			final Location l;
			if (centerEntity != null) {
				en = centerEntity.getSingle(e);
				if (en == null)
					return null;
				l = en.getLocation();
			} else {
				l = center.getSingle(e);
				if (l == null)
					return null;
				en = l.getWorld().spawn(l, ExperienceOrb.class);
			}
			final Double d = radius.getSingle(e);
			if (d == null)
				return null;
			final List<Entity> es = en.getNearbyEntities(d, d, d);
			if (centerEntity == null)
				en.remove();
			final double radiusSquared = d * d * Skript.EPSILON_MULT;
			final EntityData<?>[] ts = types.getAll(e);
			return new CheckedIterator<Entity>(es.iterator(), new Checker<Entity>() {
				@Override
				public boolean check(final Entity e) {
					if (e.getLocation().distanceSquared(l) > radiusSquared)
						return false;
					for (final EntityData<?> t : ts) {
						if (t.isInstance(e))
							return true;
					}
					return false;
				}
			});
		} else {
			if (worlds == null && returnType == Player.class)
				return super.iterator(e);
			return new NonNullIterator<Entity>() {
				
				private final World[] ws = worlds == null ? Bukkit.getWorlds().toArray(new World[0]) : worlds.getArray(e);
				private int w = -1;
				
				private final EntityData<?>[] ts = types.getAll(e);
				
				private Iterator<? extends Entity> curIter = null;
				
				@Override
				protected Entity getNext() {
					while (true) {
						while (curIter == null || !curIter.hasNext()) {
							w++;
							if (w == ws.length)
								return null;
							curIter = ws[w].getEntitiesByClass(returnType).iterator();
						}
						while (curIter.hasNext()) {
							final Entity current = curIter.next();
							for (final EntityData<?> t : ts) {
								if (t.isInstance(current))
									return current;
							}
						}
					}
				}
			};
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "all entities of types " + types.toString(e, debug) + (worlds != null ? " in " + worlds.toString(e, debug) : radius != null ? "in radius " + radius.toString(e, debug) + " around " + center.toString(e, debug) : "");
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
