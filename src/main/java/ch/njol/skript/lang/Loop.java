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

package ch.njol.skript.lang;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.event.Event;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.util.ContanerExpression;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.Container.ContainerType;
import ch.njol.util.Validate;

/**
 * A trigger section which represents a loop.
 * 
 * @author Peter Güttinger
 * @see LoopExpr
 */
public class Loop extends TriggerSection {
	
	private final Expression<?> expr;
	
	private final Map<Event, Object> current = new WeakHashMap<Event, Object>();
	private final Map<Event, Iterator<?>> currentIter = new WeakHashMap<Event, Iterator<?>>();
	
	private TriggerItem actualNext;
	
	public <T> Loop(final Expression<?> expr, final SectionNode node) {
		Validate.notNull(expr, node);
		if (Container.class.isAssignableFrom(expr.getReturnType())) {
			final ContainerType type = expr.getReturnType().getAnnotation(ContainerType.class);
			if (type == null)
				throw new SkriptAPIException(expr.getReturnType().getName() + " implements Container but is missing the required @ContainerType annotation");
			this.expr = new ContanerExpression(expr, type.value());
		} else {
			this.expr = expr;
		}
		ScriptLoader.currentLoops.add(this);
		setTriggerItems(ScriptLoader.loadItems(node));
		ScriptLoader.currentLoops.remove(ScriptLoader.currentLoops.size() - 1);
		super.setNext(this);
	}
	
	@Override
	protected TriggerItem walk(final Event e) {
		Iterator<?> iter = currentIter.get(e);
		if (iter == null) {
			iter = expr.iterator(e);
			if (iter != null && iter.hasNext())
				currentIter.put(e, iter);
		}
		if (iter == null || !iter.hasNext()) {
			debug(e, false);
			return actualNext;
		} else {
			current.put(e, iter.next());
			return walk(e, true);
		}
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return "loop " + expr.toString(e, debug);
	}
	
	public Object getCurrent(final Event e) {
		return current.get(e);
	}
	
	public Expression<?> getLoopedExpression() {
		return expr;
	}
	
	@Override
	public void setNext(final TriggerItem next) {
		actualNext = next;
	}
	
	public TriggerItem getActualNext() {
		return actualNext;
	}
	
}
