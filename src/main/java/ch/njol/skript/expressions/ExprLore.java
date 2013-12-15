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

package ch.njol.skript.expressions;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.HealthUtils;
import ch.njol.util.Kleenean;


/**
 * @author joeuguce99
 */
@SuppressWarnings("serial")
@Name("Lore")
@Description("Sets an item's lore.")
@Examples("set the item's lore 1 to \"<orange>The mighty sword Njol used\"")
@Since("2.1.1")
public class ExprLore extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprLore.class, String.class, ExpressionType.PROPERTY,
				"[the] lore %number% of [%itemstack%]");
	}
	
	private Expression<Number> lorenumber;
	private Expression<ItemStack> item;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (matchedPattern == 0)
			lorenumber = (Expression<Number>) exprs[0];
		else
			lorenumber = new SimpleLiteral<Number>(parseResult.mark, false);
		item = (Expression<ItemStack>) exprs[exprs.length - 1];
		return true;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(Event e, boolean debug) {
		return "line " + lorenumber.toString(e, debug) + " of " + item.toString(e, debug);
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.DELETE || mode == ChangeMode.SET)
			return new Class[] {String.class};
		return null;
	}
	
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		final ItemStack i = item.getSingle(e);
		if (i.getType() == Material.AIR){
			return;
		}
		final ItemMeta y = i.getItemMeta();
		final List<String> x = y.hasLore() ? y.getLore() : Lists.newArrayList("");
		final int n = lorenumber.getSingle(e).intValue();
		if (!y.hasLore()){
			x.clear();
		}
		final int z = x.size();
				switch (mode) {
					case DELETE:
						if (n>=x.size()){
							x.remove(n);
							y.setLore(x);
							i.setItemMeta(y);
						} else{
							Skript.error("Lore index is greater than the number of lores");
							return;
						}
						
						break;
					case SET:
						if (n<=z){
							x.set((n - 1), (String) delta[0]);
						} else if (n - 1 == z){
							x.add((String) delta[0]);
						} else {
							for(int l=0;l < (n-z-1); l++){
								x.add("");
							}
							x.add((String) delta[0]);
						}
						y.setLore(x);
						i.setItemMeta(y);
					break;
				}
		return;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	protected String[] get(Event e) {
		final int number = lorenumber.getSingle(e).intValue();
		final ItemMeta meta = item.getSingle(e).getItemMeta();
		final List<String> xvar = meta.getLore();
		final String st = xvar.get(number);
		return new String[] {st};
	}
}
