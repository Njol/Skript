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

package ch.njol.skript.classes;

import ch.njol.skript.api.Changer;
import ch.njol.skript.api.Converter;
import ch.njol.skript.api.Converter.ConverterUtils;
import ch.njol.util.Validate;

/**
 * @author Peter Güttinger
 * 
 */
public class ConvertedChanger<T1, T2> implements Changer<T1, T2> {
	
	private final Converter<? super T1, ?> converter;
	private final Class<?> mid;
	private final Changer<?, T2> changer;
	
	public <M> ConvertedChanger(final Converter<? super T1, ? extends M> converter, final Class<M> mid, final Changer<M, T2> changer) {
		Validate.notNull(converter, mid, changer);
		this.converter = converter;
		this.mid = mid;
		this.changer = changer;
	}
	
	@Override
	public void change(final T1[] what, final T2 delta, final ChangeMode mode) {
		ChangerUtils.change(changer, ConverterUtils.convertUnsafe(what, converter, mid), delta, mode);
	}
	
	@Override
	public Class<? extends T2> acceptChange(final ChangeMode mode) {
		return changer.acceptChange(mode);
	}
	
}
