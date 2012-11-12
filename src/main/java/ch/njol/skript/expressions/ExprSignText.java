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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;

/**
 * @author Peter Güttinger
 */
public class ExprSignText extends SimpleExpression<String> {
	private static final long serialVersionUID = -2027328055872524011L;
	
	static {
		Skript.registerExpression(ExprSignText.class, String.class, ExpressionType.PROPERTY,
				"[the] line <[1-4]> [of %block%]");//, "[the] (1st|2nd|3rd|4th) line [of %block%]");
	}
	
	private int line;
	private Expression<Block> block;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		line = Integer.parseInt(parseResult.regexes.get(0).group());
		block = (Expression<Block>) exprs[0];
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "line " + line + " of " + block.toString(e, debug);
	}
	
	@Override
	protected String[] get(final Event e) {
		if (getTime() >= 0 && block.isDefault() && e instanceof SignChangeEvent && !Delay.isDelayed(e)) {
			return new String[] {((SignChangeEvent) e).getLine(line - 1)};
		}
		final Block b = block.getSingle(e);
		if (b == null)
			return null;
		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN)
			return null;
		return new String[] {((Sign) b.getState()).getLine(line - 1)};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.CLEAR || mode == ChangeMode.SET)
			return Skript.array(String.class);
		return null;
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		if (getTime() >= 0 && block.isDefault() && e instanceof SignChangeEvent && !Delay.isDelayed(e)) {
			switch (mode) {
				case CLEAR:
					((SignChangeEvent) e).setLine(line - 1, "");
					break;
				case SET:
					((SignChangeEvent) e).setLine(line - 1, (String) delta);
					break;
			}
		}
		final Block b = block.getSingle(e);
		if (b == null)
			return;
		if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN)
			return;
		switch (mode) {
			case CLEAR:
				((Sign) b.getState()).setLine(line - 1, "");
				break;
			case SET:
				((Sign) b.getState()).setLine(line - 1, (String) delta);
				break;
		}
	}
	
	@Override
	public boolean getAnd() {
		return true;
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, SignChangeEvent.class, block);
	}
	
}
