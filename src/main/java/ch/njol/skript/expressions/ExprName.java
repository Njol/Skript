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
import org.bukkit.command.defaults.ClearCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import ch.njol.util.CollectionUtils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Name / Display Name")
@Description({"Represents a player's minecraft account name, chat display name, or playerlist name, or the custom name of an item.",
		"Please note that tab list anmes are limited to 16 characters, including colour codes which are 2 characters each."})
@Examples({"on join:",
		"	player has permission \"name.red\"",
		"	set the player's display name to \"<red>[admin]<gold>%name of player%\"",
		"	set the player's tablist name to \"<green>%name of player%\"",
		"set the name of the player's tool to \"Legendary Sword of Awesomeness\""})
@Since("1.4.6 (players' name & display name), <i>unknown</i> (player list name), 2.0 (item name)")
public class ExprName extends SimplePropertyExpression<Object, String> {
	
	final static int PLAYER = 1, ITEMSTACK = 2, ENTITY = 4;
	final static String[] types = {"players", "slots/itemstacks", "entities"};
	
	private static enum NameType {
		NAME("name", "name", PLAYER | ITEMSTACK | ENTITY, ITEMSTACK | ENTITY) {
			@Override
			void set(final Object o, final String s) {
				if (o instanceof Entity) {
					if (o instanceof LivingEntity) {
						((LivingEntity) o).setCustomName(s);
						((LivingEntity) o).setCustomNameVisible(s != null);
					}
				} else if (o instanceof ItemStack) {
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					m.setDisplayName(s);
					((ItemStack) o).setItemMeta(m);
				} else {
					assert false;
				}
			}
			
			@Override
			String get(final Object o) {
				if (o instanceof Player) {
					return ((Player) o).getName();
				} else if (o instanceof Entity) {
					if (o instanceof LivingEntity)
						return ((LivingEntity) o).getCustomName();
					return null;
				} else if (o instanceof ItemStack) {
					if (!((ItemStack) o).hasItemMeta())
						return null;
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					return m.hasDisplayName() ? m.getDisplayName() : null;
				} else {
					assert false;
					return null;
				}
			}
		},
		DISPLAY_NAME("display name", "(display|nick)[ ]name", PLAYER | ITEMSTACK | ENTITY, PLAYER | ITEMSTACK | ENTITY) {
			@Override
			void set(final Object o, final String s) {
				if (o instanceof Player) {
					((Player) o).setDisplayName(s + ChatColor.RESET);
				} else if (o instanceof Entity) {
					if (o instanceof LivingEntity) {
						((LivingEntity) o).setCustomName(s);
						((LivingEntity) o).setCustomNameVisible(s != null);
					}
				} else if (o instanceof ItemStack) {
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					m.setDisplayName(s);
					((ItemStack) o).setItemMeta(m);
				} else {
					assert false;
				}
			}
			
			@Override
			String get(final Object o) {
				if (o instanceof Player) {
					return ((Player) o).getDisplayName();
				} else if (o instanceof Entity) {
					if (o instanceof LivingEntity)
						return ((LivingEntity) o).getCustomName();
					return null;
				} else if (o instanceof ItemStack) {
					if (!((ItemStack) o).hasItemMeta())
						return null;
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					return m.hasDisplayName() ? m.getDisplayName() : null;
				} else {
					assert false;
					return null;
				}
			}
		},
		TABLIST_NAME("player list name", "(player|tab)[ ]list name", PLAYER, PLAYER) {
			@Override
			void set(final Object o, final String s) {
				if (o instanceof Player) {
					try {
						((Player) o).setPlayerListName(s.length() > 16 ? s.substring(0, 16) : s);
					} catch (final IllegalArgumentException e) {}
				} else {
					assert false;
				}
			}
			
			@Override
			String get(final Object o) {
				if (o instanceof Player) {
					return ((Player) o).getPlayerListName();
				} else {
					assert false;
					return null;
				}
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
			String r = "";
			for (int i = 0; i < types.length; i++) {
				if (i != 0 && !Skript.isRunningMinecraft(1, 4, 5)) // TODO check entity name version
					continue;
				if ((from & (1 << i)) == 0)
					continue;
				if (!r.isEmpty())
					r += "/";
				r += types[i];
			}
			return r;
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
	
	// TODO find a better method of handling this (in general)
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.DELETE && (type.acceptChange & ~PLAYER) != 0)
			return new Class[0];
		if (mode != ChangeMode.SET)
			return null;
		if ((type.acceptChange & PLAYER) != 0 && Player.class.isAssignableFrom(getExpr().getReturnType())) {
			changeType = PLAYER;
		} else if ((type.acceptChange & ITEMSTACK) != 0 && (getExpr().isSingle() && CollectionUtils.contains(getExpr().acceptChange(ChangeMode.SET), ItemStack.class) || Slot.class.isAssignableFrom(getExpr().getReturnType()))) {
			changeType = ITEMSTACK;
		} else if ((type.acceptChange & ENTITY) != 0 && Entity.class.isAssignableFrom(getExpr().getReturnType())) {
			changeType = ENTITY;
		}
		return changeType == 0 ? null : CollectionUtils.array(String.class);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		if (mode == ChangeMode.DELETE) {
			for (final Object o : getExpr().getArray(e)) {
				if (!(o instanceof Player))
					type.set(o, null);
			}
			return;
		}
		assert mode == ChangeMode.SET;
		if (changeType == PLAYER) {
			for (final Object o : getExpr().getArray(e)) {
				if (o instanceof Player)
					type.set(o, (String) delta);
			}
		} else if (changeType == ITEMSTACK) {
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
		} else if (changeType == ENTITY) {
			for (final Object o : getExpr().getArray(e)) {
				if (o instanceof Entity)
					type.set(o, (String) delta);
			}
		} else {
			assert false;
		}
	}
}
