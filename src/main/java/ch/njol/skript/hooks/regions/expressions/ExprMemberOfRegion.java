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

package ch.njol.skript.hooks.regions.expressions;

import java.util.ArrayList;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("serial")
public class ExprMemberOfRegion extends SimpleExpression<OfflinePlayer> {
	static {
		Skript.registerExpression(ExprMemberOfRegion.class, OfflinePlayer.class, ExpressionType.PROPERTY,
				"[the] (0¦members|1¦owner[s]) of [[the] region[s]] %regions%");
	}
	
	private boolean owners;
	private Expression<Region> regions;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		regions = (Expression<Region>) exprs[0];
		owners = parseResult.mark == 1;
		return true;
	}
	
	@Override
	protected OfflinePlayer[] get(final Event e) {
		final ArrayList<OfflinePlayer> r = new ArrayList<OfflinePlayer>();
		for (final Region region : regions.getArray(e)) {
			r.addAll(owners ? region.getOwners() : region.getMembers());
		}
		return r.toArray(new OfflinePlayer[r.size()]);
	}
	
	@Override
	public boolean isSingle() {
		return owners && regions.isSingle() && !RegionsPlugin.hasMultipleOwners();
	}
	
	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "the " + (owners ? "owner" + (isSingle() ? "" : "s") : "members") + " of " + regions.toString(e, debug);
	}
	
}
