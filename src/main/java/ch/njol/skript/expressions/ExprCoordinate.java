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

import org.bukkit.Location;
import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.CollectionUtils;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
@Name("Coordinate")
@Description("Represents a given coordinate of a location. ")
@Examples({"player's y-coordinate is smaller than 40:",
		"	message \"Watch out for lava!\""})
@Since("1.4.3")
public class ExprCoordinate extends SimplePropertyExpression<Location, Double> {
	static {
		register(ExprCoordinate.class, Double.class, "(0¦x|1¦y|2¦z)(-| )(coord[inate]|pos[ition]|loc[ation])[s]", "locations");
	}
	
	private final static char[] axes = {'x', 'y', 'z'};
	
	private int axis;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		axis = parseResult.mark;
		return true;
	}
	
	@Override
	public Double convert(final Location l) {
		return axis == 0 ? l.getX() : axis == 1 ? l.getY() : l.getZ();
	}
	
	@Override
	protected String getPropertyName() {
		return "the " + axes[axis] + "-coordinate";
	}
	
	@Override
	public Class<Double> getReturnType() {
		return Double.class;
	}
	
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) && getExpr().isSingle() && CollectionUtils.contains(getExpr().acceptChange(ChangeMode.SET), Location.class))
			return new Class[] {Number.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		final Location l = getExpr().getSingle(e);
		if (l == null)
			return;
		double n = ((Number) delta).doubleValue();
		switch (mode) {
			case REMOVE:
				n = -n;
				//$FALL-THROUGH$
			case ADD:
				if (axis == 0) {
					l.setX(l.getX() + n);
				} else if (axis == 1) {
					l.setY(l.getY() + n);
				} else {
					l.setZ(l.getZ() + n);
				}
				getExpr().change(e, l, ChangeMode.SET);
				break;
			case SET:
				if (axis == 0) {
					l.setX(n);
				} else if (axis == 1) {
					l.setY(n);
				} else {
					l.setZ(n);
				}
				getExpr().change(e, l, ChangeMode.SET);
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}
	
}
