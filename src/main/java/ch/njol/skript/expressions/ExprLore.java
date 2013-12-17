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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

/**
 * @author joeuguce99
 */
@SuppressWarnings("serial")
@Name("Lore")
@Description("Sets an item's lore.")
@Examples("set the item's lore 1 to \"<orange>The mighty sword Njol used\"")
@Since("2.1")
public class ExprLore extends SimpleExpression<String> {
	static {
		try {
			ItemMeta.class.getName();
			
			Skript.registerExpression(ExprLore.class, String.class, ExpressionType.PROPERTY,
					"[the] lore of [%itemstack/itemtype%]", "%itemstack/itemtype%'[s] lore");
			
		} catch (final NoClassDefFoundError e) {}
	}
	
	private Expression<ItemStack> item;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		item = (Expression<ItemStack>) exprs[exprs.length - 1];
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the lore of " + item.toString(e, debug);
	}
	
	@Override
	protected String[] get(final Event e) {
		final Object i = item.getSingle(e);
		if (i == null || i instanceof ItemStack && ((ItemStack) i).getType() == Material.AIR)
			return null;
		final ItemMeta meta = i instanceof ItemStack ? ((ItemStack) i).getItemMeta() : (ItemMeta) ((ItemType) i).getItemMeta();
		if (!meta.hasLore())
			return null;
		return new String[] {StringUtils.join(meta.getLore(), "\n")};
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case SET:
			case DELETE:
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
				return new Class[] {String.class};
			case RESET:
			default:
				return null;
		}
	}
	
	@Override
	public void change(final Event e, final Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		final Object i = item.getSingle(e);
		if (i == null || i instanceof ItemStack && ((ItemStack) i).getType() == Material.AIR)
			return;
		final ItemMeta meta = i instanceof ItemStack ? ((ItemStack) i).getItemMeta() : (ItemMeta) ((ItemType) i).getItemMeta();
		switch (mode) {
			case SET:
				meta.setLore(Arrays.asList(((String) delta[0]).split("\n")));
				break;
			case DELETE:
				meta.setLore(null);
				break;
			case ADD: {
				final List<String> l = meta.hasLore() ? meta.getLore() : Arrays.asList("");
				l.set(l.size() - 1, l.get(l.size() - 1) + (String) delta[0]);
				meta.setLore(l);
				break;
			}
			case REMOVE:
			case REMOVE_ALL: {
				String l = meta.hasLore() ? StringUtils.join(meta.getLore(), "\n") : "";
				if (SkriptConfig.caseSensitive.value()) {
					l = mode == ChangeMode.REMOVE ? l.replaceFirst(Pattern.quote((String) delta[0]), "") : l.replace((CharSequence) delta[0], "");
				} else {
					final Matcher m = Pattern.compile(Pattern.quote((String) delta[0]), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(l);
					l = mode == ChangeMode.REMOVE ? m.replaceFirst("") : m.replaceAll("");
				}
				meta.setLore(l.isEmpty() ? null : Arrays.asList(l.split("\n")));
				break;
			}
			case RESET:
				break;
		}
		if (i instanceof ItemStack)
			((ItemStack) i).setItemMeta(meta);
		else
			((ItemType) i).setItemMeta(meta);
		return;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
