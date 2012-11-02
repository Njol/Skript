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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import ch.njol.skript.Skript;
import ch.njol.skript.Skript.ExpressionType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;

/**
 * @author Peter Güttinger
 */
public class ExprFacing extends PropertyExpression<Object, BlockFace> {
	private static final long serialVersionUID = -1100508280427464204L;
	
	static {
		Skript.registerExpression(ExprFacing.class, BlockFace.class, ExpressionType.PROPERTY,
				"[the] facing of %livingentity/block%", "%livingentity/block%'[s] facing",
				"[the] horizontal facing of %livingentity/block%", "%livingentity/block%'[s] horizontal facing");
	}
	
	private boolean horizontal;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final int isDelayed, final ParseResult parseResult) {
		setExpr(exprs[0]);
		horizontal = matchedPattern >= 2;
		return true;
	}
	
	@Override
	protected BlockFace[] get(final Event e, final Object[] source) {
		return get(source, new Converter<Object, BlockFace>() {
			@Override
			public BlockFace convert(final Object o) {
				if (o instanceof Block) {
					final MaterialData d = ((Block) o).getType().getNewData(((Block) o).getData());
					if (d instanceof Directional)
						return ((Directional) d).getFacing();
					return null;
				} else if (e instanceof LivingEntity) {
					return Utils.getFacing(((LivingEntity) e).getLocation(), horizontal);
				}
				assert false;
				return null;
			}
		});
	}
	
	@Override
	public Class<BlockFace> getReturnType() {
		return BlockFace.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "facing of " + getExpr();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (!Block.class.isAssignableFrom(getExpr().getReturnType()))
			return null;
		if (mode == ChangeMode.SET)
			return Skript.array(BlockFace.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final Object delta, final ChangeMode mode) throws UnsupportedOperationException {
		final Block b = (Block) getExpr().getSingle(e);
		if (b == null)
			return;
		final MaterialData d = b.getType().getNewData(b.getData());
		if (!(d instanceof Directional))
			return;
		((Directional) d).setFacingDirection((BlockFace) delta);
		b.setData(d.getData());
	}
	
}
