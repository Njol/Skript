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

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Math2;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Data Value")
@Description({"The data value of an item.",
		"You usually don't need this expression as you can check and set items with aliases easily, " +
				"but this expression can e.g. be used to \"add 1 to data of &lt;item&gt;\", e.g. for cycling through all wool colours."})
@Examples({"add 1 to the data value of the clicked block"})
@Since("1.2")
public class ExprDurability extends SimplePropertyExpression<ItemStack, Short> {
	
	static {
		register(ExprDurability.class, Short.class, "((data|damage)[s] [value[s]]|durabilit(y|ies))", "itemstacks");
	}
	
	@Override
	public Short convert(final ItemStack is) {
		return is.getDurability();
	}
	
	@Override
	public String getPropertyName() {
		return "data";
	}
	
	@Override
	public Class<Short> getReturnType() {
		return Short.class;
	}
	
//	@Override
//	public void change(Event e, final Changer2<Number> changer) throws UnsupportedOperationException {
//		getExpr().change(e, new Changer2<ItemStack>() {
//			@Override
//			public ItemStack change(ItemStack i) {
//				i.setDurability(changer.change(i.getDurability()).shortValue());
//				return i;
//			}
//		});
//	}
	
//	private Expression<? extends Slot> slots = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
//		final SimpleLog log = SkriptLogger.startSubLog();
//		if ((slots = getExpr().getConvertedExpression(Slot.class)) != null) {
//			log.stop();
//			return Skript.array(Number.class);
//		}
//		log.stop();
		if (getExpr().isSingle() && Utils.contains(getExpr().acceptChange(ChangeMode.SET), ItemStack.class))
			return Utils.array(Number.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) {
		int a = 0;
		if (mode != ChangeMode.DELETE)
			a = ((Number) delta).intValue();
		switch (mode) {
			case REMOVE:
				a = -a;
				//$FALL-THROUGH$
			case ADD:
//				if (slots != null) {
//					for (final Slot slot : slots.getArray(e)) {
//						final ItemStack item = slot.getItem();
//						item.setDurability((short) Math2.fit(0, item.getDurability() + a, Skript.MAXDATAVALUE));
//						slot.setItem(item);
//					}
//				} else {
				final ItemStack is = getExpr().getSingle(e);
				if (is == null)
					return;
				is.setDurability((short) Math2.fit(0, is.getDurability() + a, Skript.MAXDATAVALUE));
				getExpr().change(e, is, ChangeMode.SET);
//				}
				break;
			case DELETE:
				a = 0;
				//$FALL-THROUGH$
			case SET:
//				if (slots != null) {
//					for (final Slot slot : slots.getArray(e)) {
//						final ItemStack item = slot.getItem();
//						item.setDurability((short) a);
//						slot.setItem(item);
//					}
//				} else {
				final ItemStack is2 = getExpr().getSingle(e);
				if (is2 == null)
					return;
				is2.setDurability((short) a);
				getExpr().change(e, is2, ChangeMode.SET);
//				}
				break;
		}
	}
	
}
