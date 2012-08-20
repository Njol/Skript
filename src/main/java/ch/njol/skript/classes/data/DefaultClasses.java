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

package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.NumberArithmetic;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 * 
 */
public class DefaultClasses {
	
	public DefaultClasses() {}
	
	public final static int VARIABLENAME_NUMBERACCURACY = 8;
	
	static {
		Skript.registerClass(new ClassInfo<Object>(Object.class, "object", "object"));
		
		Skript.registerClass(new ClassInfo<Number>(Number.class, "number", "number")
				.parser(new Parser<Number>() {
					@Override
					public Number parse(final String s, final ParseContext context) {
						try {
							return Integer.valueOf(s);
						} catch (final NumberFormatException e) {}
						try {
							return Double.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Number n) {
						return StringUtils.toString(n.doubleValue(), Skript.NUMBERACCURACY);
					}
					
					@Override
					public String toCodeString(final Number n) {
						return StringUtils.toString(n.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
				}));
		
		Skript.registerClass(new ClassInfo<Integer>(Integer.class, "integer", "integer")
				.user("integers?")
				.defaultExpression(new SimpleLiteral<Integer>(1, true))
				.parser(new Parser<Integer>() {
					@Override
					public Integer parse(final String s, final ParseContext context) {
						try {
							return Integer.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Integer i) {
						return "" + i;
					}
					
					@Override
					public String toCodeString(final Integer i) {
						return "" + i;
					}
				}).serializer(new Serializer<Integer>() {
					
					@Override
					public String serialize(final Integer i) {
						return "" + i;
					}
					
					@Override
					public Integer deserialize(final String s) {
						try {
							return Integer.parseInt(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}).math(Double.class, new NumberArithmetic<Integer>()));
		
		Skript.registerClass(new ClassInfo<Double>(Double.class, "double", "number")
				.user("numbers?")
				.defaultExpression(new SimpleLiteral<Double>(1., true))
				.parser(new Parser<Double>() {
					@Override
					public Double parse(final String s, final ParseContext context) {
						try {
							return Double.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Double d) {
						return StringUtils.toString(d, Skript.NUMBERACCURACY);
					}
					
					@Override
					public String toCodeString(final Double d) {
						return StringUtils.toString(d.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
				}).serializer(new Serializer<Double>() {
					
					@Override
					public String serialize(final Double d) {
						return "" + d;
					}
					
					@Override
					public Double deserialize(final String s) {
						try {
							return Double.parseDouble(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}).math(Double.class, new NumberArithmetic<Double>()));
		
		Skript.registerClass(new ClassInfo<Float>(Float.class, "float", "number")
				.defaultExpression(new SimpleLiteral<Float>(1f, true))
				.parser(new Parser<Float>() {
					@Override
					public Float parse(final String s, final ParseContext context) {
						try {
							return Float.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Float f) {
						return StringUtils.toString(f, Skript.NUMBERACCURACY);
					}
					
					@Override
					public String toCodeString(final Float f) {
						return StringUtils.toString(f.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
				}).serializer(new Serializer<Float>() {
					@Override
					public String serialize(final Float f) {
						return "" + f;
					}
					
					@Override
					public Float deserialize(final String s) {
						try {
							return Float.parseFloat(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}).math(Double.class, new NumberArithmetic<Float>()));
		
		Skript.registerClass(new ClassInfo<Boolean>(Boolean.class, "boolean", "boolean")
				.parser(new Parser<Boolean>() {
					@Override
					public Boolean parse(final String s, final ParseContext context) {
						if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes"))
							return Boolean.TRUE;
						if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no"))
							return Boolean.FALSE;
						return null;
					}
					
					@Override
					public String toString(final Boolean b) {
						return b.toString();
					}
					
					@Override
					public String toCodeString(final Boolean b) {
						return "" + b;
					}
				}).serializer(new Serializer<Boolean>() {
					@Override
					public String serialize(final Boolean b) {
						return "" + b;
					}
					
					@Override
					public Boolean deserialize(final String s) {
						if (s.equals("true"))
							return Boolean.TRUE;
						if (s.equals("false"))
							return Boolean.FALSE;
						return null;
					}
				}));
		
		Skript.registerClass(new ClassInfo<Byte>(Byte.class, "byte", "integer")
				.defaultExpression(new SimpleLiteral<Byte>((byte) 1, true))
				.parser(new Parser<Byte>() {
					@Override
					public Byte parse(final String s, final ParseContext context) {
						try {
							return Byte.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Byte b) {
						return "" + b;
					}
					
					@Override
					public String toCodeString(final Byte b) {
						return "" + b;
					}
				}).serializer(new Serializer<Byte>() {
					
					@Override
					public String serialize(final Byte b) {
						return "" + b;
					}
					
					@Override
					public Byte deserialize(final String s) {
						try {
							return Byte.parseByte(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}).math(Double.class, new NumberArithmetic<Byte>()));
		
		Skript.registerClass(new ClassInfo<Short>(Short.class, "short", "integer")
				.defaultExpression(new SimpleLiteral<Short>((short) 1, true))
				.parser(new Parser<Short>() {
					@Override
					public Short parse(final String s, final ParseContext context) {
						try {
							return Short.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Short s) {
						return "" + s;
					}
					
					@Override
					public String toCodeString(final Short s) {
						return "" + s;
					}
				}).serializer(new Serializer<Short>() {
					
					@Override
					public String serialize(final Short s) {
						return "" + s;
					}
					
					@Override
					public Short deserialize(final String s) {
						try {
							return Short.parseShort(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}).math(Double.class, new NumberArithmetic<Short>()));
		
		Skript.registerClass(new ClassInfo<Long>(Long.class, "long", "integer")
				.defaultExpression(new SimpleLiteral<Long>((long) 1, true))
				.parser(new Parser<Long>() {
					@Override
					public Long parse(final String s, final ParseContext context) {
						try {
							return Long.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Long l) {
						return "" + l;
					}
					
					@Override
					public String toCodeString(final Long l) {
						return "" + l;
					}
				}).serializer(new Serializer<Long>() {
					
					@Override
					public String serialize(final Long l) {
						return "" + l;
					}
					
					@Override
					public Long deserialize(final String s) {
						try {
							return Long.parseLong(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}).math(Double.class, new NumberArithmetic<Long>()));
		
		Skript.registerClass(new ClassInfo<String>(String.class, "string", "text")
				.user("text")
				.parser(new Parser<String>() {
					@Override
					public String parse(final String s, final ParseContext context) {
						if (context == ParseContext.COMMAND && !s.isEmpty())
							return s;
						else if (context == ParseContext.CONFIG && s.startsWith("\"") && s.endsWith("\""))
							return s.substring(1, s.length() - 1);
						return null;
					}
					
					@Override
					public String toString(final String s) {
						return s;
					}
					
					@Override
					public String getDebugMessage(final String s) {
						return '"' + s + '"';
					}
					
					@Override
					public String toCodeString(final String o) {
						return "" + o;
					}
				}).serializer(new Serializer<String>() {
					@Override
					public String serialize(final String s) {
						return s;
					}
					
					@Override
					public String deserialize(final String s) {
						return s;
					}
				}));
	}
}
