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

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.api.Changer.ChangeMode;
import ch.njol.skript.classes.DefaultChangers;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SimpleExpression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Offset;

/**
 * @author Peter Güttinger
 * 
 */
public class ExprBlock extends SimpleExpression<Block> {
	
	static {
		Skript.registerExpression(ExprBlock.class, Block.class, ExpressionType.SIMPLE, "[the] [event-]block");
		Skript.registerExpression(ExprBlock.class, Block.class, ExpressionType.NORMAL, "[the] block %offset% [%block%]");
	}
	
	private Expression<Offset> offset = null;
	private Expression<Block> block;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final ParseResult parser) {
		if (exprs.length > 0) {
			offset = (Expression<Offset>) exprs[0];
			block = (Expression<Block>) exprs[1];
		} else {
			block = new EventValueExpression<Block>(Block.class);
			((EventValueExpression<Block>) block).init();
		}
		return true;
	}
	
	@Override
	protected Block[] get(final Event e) {
		if (offset == null)
			return block.getArray(e);
		final Offset o = offset.getSingle(e);
		if (o == null)
			return null;
		return new Block[] {o.getRelative(block.getSingle(e))};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return offset == null ? "the block" : "the block " + offset.toString(e, debug) + " " + block.toString(e, debug);
	}
	
	@Override
	public boolean isDefault() {
		return offset == null;
	}
	
	@Override
	public boolean setTime(final int time) {
		if (offset != null)
			return false;
		return block.setTime(time);
	}
	
	@Override
	public int getTime() {
		return block.getTime();
	}
	
	@Override
	public boolean getAnd() {
		return block.getAnd();
	}
	
	@Override
	public Class<?> acceptChange(final ChangeMode mode) {
		return DefaultChangers.blockChanger.acceptChange(mode);
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		if (getTime() >= 0 && offset == null) {
			
		}
		DefaultChangers.blockChanger.change(getArray(e), delta, mode);
	}
	
}
