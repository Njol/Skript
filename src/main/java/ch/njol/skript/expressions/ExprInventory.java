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

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
public class ExprInventory extends PropertyExpression<InventoryHolder, Inventory> {
	private static final long serialVersionUID = 1029322639581603342L;
	
	static {
		Skript.registerExpression(ExprInventory.class, Inventory.class, ExpressionType.PROPERTY, "[the] inventor(y|ies) of %inventoryholders%", "%inventoryholders%'[s] inventor(y|ies)");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<InventoryHolder>) vars[0]);
		return true;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the inventor" + (getExpr().isSingle() ? "y" : "ies") + " of " + getExpr().toString(e, debug);
	}
	
	@Override
	protected Inventory[] get(final Event e, final InventoryHolder[] source) {
		return get(source, new Getter<Inventory, InventoryHolder>() {
			@Override
			public Inventory get(final InventoryHolder h) {
				return h.getInventory();
			}
		});
	}
	
	@Override
	public Class<Inventory> getReturnType() {
		return Inventory.class;
	}
	
}
