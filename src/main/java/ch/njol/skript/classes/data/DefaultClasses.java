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

package ch.njol.skript.classes.data;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.IntegerArithmetic;
import ch.njol.skript.classes.NumberArithmetic;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.lang.util.VariableString;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.StringUtils;

/**
 * @author Peter Güttinger
 */
public class DefaultClasses {
	public DefaultClasses() {}
	
	public final static int VARIABLENAME_NUMBERACCURACY = 8;
	
	static {
		Classes.registerClass(new ClassInfo<Object>(Object.class, "object")
				.name("Object")
				.description("The supertype of all types, meaning that if %object% is used in e.g. a condition it will accept all kinds of expressions.")
				.usage("")
				.examples("")
				.since("1.0"));
		
		Classes.registerClass(new ClassInfo<Number>(Number.class, "number")
				.user("num(ber)?s?")
				.name("Number")
				.description("A number, e.g. 2.5, 3, or -9812454.",
						"Please note that many expressions only need integers, i.e. will discard any frational parts of any numbers without producing an error.")
				.usage("<code>[-]###[.###]</code> (any amount of digits, very big numbers will be truncated though)")
				.examples("set the player's health to 5.5",
						"set {_temp} to 2*{_temp} - 2.5")
				.since("1.0")
				// is registered after all other number classes
				.parser(new Parser<Number>() {
					@Override
					public Number parse(final String s, final ParseContext context) {
						try {
							return Integer.valueOf(s);
						} catch (final NumberFormatException e) {}
						try {
							return s.endsWith("%") ? Double.parseDouble(s.substring(0, s.length() - 1)) / 100 : Double.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Number n) {
						return StringUtils.toString(n.doubleValue(), SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(final Number n) {
						return StringUtils.toString(n.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+(\\.\\d+)?";
					}
				}).serializer(new Serializer<Number>() {
					
					@Override
					public String serialize(final Number n) {
						return "" + n;
					}
					
					@Override
					public Number deserialize(final String s) {
						try {
							return Integer.valueOf(s);
						} catch (final NumberFormatException e) {}
						try {
							return Double.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
		Classes.registerClass(new ClassInfo<Long>(Long.class, "long")
				.user("int(eger)?s?")
				.before("integer", "short", "byte")
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
					public String toVariableNameString(final Long l) {
						return "" + l;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+";
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
		
		Classes.registerClass(new ClassInfo<Integer>(Integer.class, "integer")
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
					public String toVariableNameString(final Integer i) {
						return "" + i;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+";
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
				}).math(Integer.class, new IntegerArithmetic<Integer>()));
		
		Classes.registerClass(new ClassInfo<Double>(Double.class, "double")
				.defaultExpression(new SimpleLiteral<Double>(1., true))
				.after("long")
				.before("float", "integer", "short", "byte")
				.parser(new Parser<Double>() {
					@Override
					public Double parse(final String s, final ParseContext context) {
						try {
							return s.endsWith("%") ? Double.parseDouble(s.substring(0, s.length() - 1)) / 100 : Double.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Double d) {
						return StringUtils.toString(d, SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(final Double d) {
						return StringUtils.toString(d.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+(\\.\\d+)?";
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
		
		Classes.registerClass(new ClassInfo<Float>(Float.class, "float")
				.defaultExpression(new SimpleLiteral<Float>(1f, true))
				.parser(new Parser<Float>() {
					@Override
					public Float parse(final String s, final ParseContext context) {
						try {
							return s.endsWith("%") ? Float.parseFloat(s.substring(0, s.length() - 1)) / 100 : Float.valueOf(s);
						} catch (final NumberFormatException e) {
							return null;
						}
					}
					
					@Override
					public String toString(final Float f) {
						return StringUtils.toString(f, SkriptConfig.numberAccuracy.value());
					}
					
					@Override
					public String toVariableNameString(final Float f) {
						return StringUtils.toString(f.doubleValue(), VARIABLENAME_NUMBERACCURACY);
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+(\\.\\d+)?";
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
		
		Classes.registerClass(new ClassInfo<Boolean>(Boolean.class, "boolean")
				.user("booleans?")
				.name("Boolean")
				.description("A boolean is a value that is either true or false. Other accepted names are 'on' and 'yes' for true, and 'off' and 'no' for false.")
				.usage("true/yes/on or false/no/off")
				.examples("set {config.%player%.use mod} to false")
				.since("1.0")
				.parser(new Parser<Boolean>() {
					@Override
					public Boolean parse(final String s, final ParseContext context) {
						if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("on"))
							return Boolean.TRUE;
						if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("off"))
							return Boolean.FALSE;
						return null;
					}
					
					@Override
					public String toString(final Boolean b) {
						return b.toString();
					}
					
					@Override
					public String toVariableNameString(final Boolean b) {
						return "" + b;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "(true|false)";
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
		
		Classes.registerClass(new ClassInfo<Short>(Short.class, "short")
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
					public String toVariableNameString(final Short s) {
						return "" + s;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+";
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
				}).math(Integer.class, new IntegerArithmetic<Short>()));
		
		Classes.registerClass(new ClassInfo<Byte>(Byte.class, "byte")
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
					public String toVariableNameString(final Byte b) {
						return "" + b;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "-?\\d+";
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
				}).math(Integer.class, new IntegerArithmetic<Byte>()));
		
		Classes.registerClass(new ClassInfo<String>(String.class, "string")
				.user("texts?")
				.name("Text")
				.description("Text is simply text, i.e. a sequence of characters, which can optionally contain expressions which will be replaced with a meaningful representation " +
						"(e.g. %player% will be replaced with the player's name).",
						"Because scripts are also text, you have to put text into double quotes to tell Skript which part of the line is an effect/expression and which part is the text.",
						"Please read the article on <a href='../strings'>Texts and Variable Names</a> to learn more.")
				.usage("simple: <code>\"...\"</code>",
						"quotes: <code>\"...\"\"...\"</code>",
						"expressions: <code>\"...%expression%...\"</code>",
						"percent signs: <code>\"...%%...\"</code>")
				.examples("broadcast \"Hello World!\"",
						"message \"Hello %player%\"",
						"message \"The id of \"\"%type of tool%\"\" is %id of tool%.\"")
				.since("1.0")
				.parser(new Parser<String>() {
					@Override
					public String parse(final String s, final ParseContext context) {
						switch (context) {
							case DEFAULT:
								throw new SkriptAPIException("Strings must not be parsed as DEFAULT using it's Parser, but by parsing it as a VariableString!");
							case COMMAND:
								return s;
							case CONFIG:
								if (!VariableString.isQuotedCorrectly(s, true))
									return null;
								return Utils.replaceChatStyles(s.substring(1, s.length() - 1).replace("\"\"", "\""));
							case EVENT:
								if (VariableString.isQuotedCorrectly(s, true))
									return Utils.replaceChatStyles(s.substring(1, s.length() - 1).replace("\"\"", "\""));
								return Utils.replaceChatStyles(s);
							default:
								return null;
						}
					}
					
					@Override
					public boolean canParse(final ParseContext context) {
						return context != ParseContext.DEFAULT;
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
					public String toVariableNameString(final String o) {
						return "" + o;
					}
					
					@Override
					public String getVariableNamePattern() {
						return ".+";
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
