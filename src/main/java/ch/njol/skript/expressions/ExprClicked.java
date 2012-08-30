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

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import ch.njol.skript.Aliases;
import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.SubLog;
import ch.njol.skript.util.ItemType;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class ExprClicked extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprClicked.class, Object.class, ExpressionType.SIMPLE, "[the] clicked <.+>");
	}
	
	private EntityData<?> entityType = null;
	/**
	 * null for any block
	 */
	private ItemType itemType = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		final String s = parseResult.regexes.get(0).group();
		final SubLog log = SkriptLogger.startSubLog();
		entityType = EntityData.parse(s);
		if (entityType == null) {
			log.clear();
			if (!(s.equalsIgnoreCase("block") || s.equalsIgnoreCase("any block") || s.equalsIgnoreCase("a block"))) {
				itemType = Aliases.parseItemType(s);
				log.stop();
				if (itemType == null) {
					Skript.error("'" + s + "' is neither an entity type nor an item type");
					return false;
				}
			}
			log.stop();
			log.printLog();
			if (!Utils.contains(ScriptLoader.currentEvents, PlayerInteractEvent.class)) {
				Skript.error("The expression 'clicked block' can only be used in a click event");
				return false;
			}
		} else {
			log.stop();
			log.printLog();
			if (!Utils.containsAny(ScriptLoader.currentEvents, PlayerInteractEntityEvent.class, EntityDamageByEntityEvent.class)) {
				Skript.error("The expression '" + parseResult.expr + "' can only be used in a click event");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Object> getReturnType() {
		return entityType != null ? entityType.getType() : Block.class;
	}
	
	@Override
	protected Object[] get(final Event e) {
		if (e instanceof PlayerInteractEvent) {
			if (entityType != null)
				return null;
			final Block b = ((PlayerInteractEvent) e).getClickedBlock();
			if (itemType == null || itemType.isOfType(b))
				return new Block[] {b};
			return null;
		} else {
			if (entityType == null)
				return null;
			final Entity en;
			if (e instanceof PlayerInteractEntityEvent) {
				en = Utils.validate(((PlayerInteractEntityEvent) e).getRightClicked());
			} else {
				if (!(((EntityDamageByEntityEvent) e).getDamager() instanceof Player))
					return null;
				en = Utils.validate(((EntityDamageByEntityEvent) e).getEntity());
			}
			if (entityType.isInstance(en)) {
				final Entity[] one = (Entity[]) Array.newInstance(entityType.getType(), 1);
				one[0] = en;
				return one;
			}
			return null;
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "clicked " + (entityType != null ? entityType : itemType != null ? itemType : "block");
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
}
