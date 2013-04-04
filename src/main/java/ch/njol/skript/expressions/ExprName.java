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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Name / Display Name")
@Description("Represents a player's minecraft account name, chat display name, or playerlist name, or the custom name of an item.")
@Examples({"on join:",
		"	player has permission \"name.red\"",
		"	set the player's display name to \"<red>%name of player%\"",
		"	set the player's tablist name to \"<red>%name of player%\"",
		"set the name of the player's tool to \"Legendary Sword of Awesomeness\""})
@Since("1.4.6 (players' name & display name), <i>unknown</i> (player list name), 2.0 (item name)")
public class ExprName extends SimplePropertyExpression<Object, String> {
	
	final static int PLAYER = 1, ITEMSTACK = 2;
	
	private static enum NameType {
		NAME("name", "name", PLAYER | ITEMSTACK, ITEMSTACK) {
			@Override
			void set(final Object o, final String s) {
				final ItemMeta m = ((ItemStack) o).getItemMeta();
				m.setDisplayName(s);
				((ItemStack) o).setItemMeta(m);
			}
			
			@Override
			String get(final Object o) {
				if (o instanceof Player) {
					return ((Player) o).getName();
				} else {
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					return m.hasDisplayName() ? m.getDisplayName() : null;
				}
			}
		},
		DISPLAY_NAME("display name", "(display|nick)[ ]name", PLAYER | ITEMSTACK, PLAYER | ITEMSTACK) {
			@Override
			void set(final Object o, final String s) {
				if (o instanceof Player) {
					((Player) o).setDisplayName(s + ChatColor.RESET);
				} else {
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					m.setDisplayName(s);
					((ItemStack) o).setItemMeta(m);
				}
			}
			
			@Override
			String get(final Object o) {
				if (o instanceof Player) {
					return ((Player) o).getDisplayName();
				} else {
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					return m.hasDisplayName() ? m.getDisplayName() : null;
				}
			}
		},
		TABLIST_NAME("player list name", "(player|tab)[ ]list name", PLAYER, PLAYER) {
			@Override
			void set(final Object o, final String s) {
				try {
					((Player) o).setPlayerListName(s.length() > 16 ? s.substring(0, 16) : s);
				} catch (final IllegalArgumentException e) {}
			}
			
			@Override
			String get(final Object o) {
				return ((Player) o).getPlayerListName();
			}
		};
		
		final String name;
		final String pattern;
		final int from;
		final int acceptChange;
		
		NameType(final String name, final String pattern, final int from, final int change) {
			this.name = name;
			this.pattern = "(" + ordinal() + "¦)" + pattern;
			this.from = from;
			acceptChange = change;
		}
		
		abstract void set(Object o, String s);
		
		abstract String get(Object o);
		
		String getFrom() {
			if (from == ITEMSTACK) {
				if (!Skript.isRunningMinecraft(1, 4, 5))
					return null;
				return "slots/itemstacks";
			} else if (from == PLAYER || !Skript.isRunningMinecraft(1, 4, 5)) {
				return "players";
			} else {
				return "players/slots/itemstacks";
			}
		}
	}
	
	static {
		for (final NameType n : NameType.values()) {
			if (n.getFrom() != null)
				register(ExprName.class, String.class, n.pattern, n.getFrom());
		}
	}
	
	private NameType type;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = NameType.values()[parseResult.mark];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
	@Override
	protected String getPropertyName() {
		return type.name;
	}
	
	@Override
	public String convert(final Object o) {
		return type.get(o instanceof Slot ? ((Slot) o).getItem() : o);
	}
	
	private int changeType = 0;
	
	// TODO find a better method of handling this
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET && (type.acceptChange & PLAYER) == PLAYER && !ItemStack.class.isAssignableFrom(getExpr().getReturnType())) {
			changeType = PLAYER;
			return Utils.array(String.class);
		} else if (mode == ChangeMode.SET && (type.acceptChange & ITEMSTACK) == ITEMSTACK &&
				(getExpr().isSingle() && Utils.contains(getExpr().acceptChange(ChangeMode.SET), ItemStack.class) || Slot.class.isAssignableFrom(getExpr().getReturnType()))) {
			changeType = ITEMSTACK;
			return Utils.array(String.class);
		}
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		if (changeType == PLAYER) {
			for (final Object o : getExpr().getArray(e)) {
				if (o instanceof Player)
					type.set(o, (String) delta);
			}
		} else {
			if (Slot.class.isAssignableFrom(getExpr().getReturnType())) {
				for (final Slot s : (Slot[]) getExpr().getArray(e)) {
					final ItemStack i = s.getItem();
					type.set(i, (String) delta);
					s.setItem(i);
				}
			} else {
				final Object i = getExpr().getSingle(e);
				if (i instanceof Player)
					return;
				type.set(i instanceof Slot ? ((Slot) i).getItem() : i, (String) delta);
				getExpr().change(e, i, ChangeMode.SET);
			}
		}
	}
}
