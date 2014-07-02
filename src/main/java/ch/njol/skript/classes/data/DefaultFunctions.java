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

import org.bukkit.Location;
import org.bukkit.World;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Math2;

/**
 * FIXME generate/write documentation
 * 
 * @author Peter Güttinger
 */
@SuppressWarnings("null")
public class DefaultFunctions {
	public DefaultFunctions() {}
	
	static {
		
		final ClassInfo<Number> numberClass = Classes.getExactClassInfo(Number.class);
		final ClassInfo<Long> longClass = Classes.getExactClassInfo(Long.class);
		
		final Parameter<?>[] numberParam = new Parameter[] {new Parameter<Number>("n", numberClass, true, null)};
		final Parameter<?>[] numbersParam = new Parameter[] {new Parameter<Number>("ns", numberClass, false, null)};
		
		// basic math functions
		
		Functions.registerFunction(new JavaFunction<Long>("floor", numberParam, longClass, true) {
			@Override
			public Long[] execute(final FunctionEvent e, final Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.floor(((Number) params[0][0]).doubleValue())};
			}
		});
		Functions.registerFunction(new JavaFunction<Long>("round", numberParam, longClass, true) {
			@Override
			public Long[] execute(final FunctionEvent e, final Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.round(((Number) params[0][0]).doubleValue())};
			}
		});
		Functions.registerFunction(new JavaFunction<Long>("ceil", numberParam, longClass, true) {
			@Override
			public Long[] execute(final FunctionEvent e, final Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.ceil(((Number) params[0][0]).doubleValue())};
			}
		});
		Functions.registerFunction(new JavaFunction<Long>("ceiling", numberParam, longClass, true) {
			@Override
			public Long[] execute(final FunctionEvent e, final Object[][] params) {
				if (params[0][0] instanceof Long)
					return new Long[] {(Long) params[0][0]};
				return new Long[] {Math2.ceil(((Number) params[0][0]).doubleValue())};
			}
		});
		
		Functions.registerFunction(new JavaFunction<Number>("abs", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Number n = (Number) params[0][0];
				if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long)
					return new Long[] {Math.abs(n.longValue())};
				return new Double[] {Math.abs(n.doubleValue())};
			}
		});
		
		Functions.registerFunction(new JavaFunction<Number>("exp", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.exp(((Number) params[0][0]).doubleValue())};
			}
		});
		Functions.registerFunction(new JavaFunction<Number>("ln", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.log(((Number) params[0][0]).doubleValue())};
			}
		});
		Functions.registerFunction(new JavaFunction<Number>("log", new Parameter[] {new Parameter<Number>("n", numberClass, true, null), new Parameter<Number>("base", numberClass, true, new SimpleLiteral<Number>(10, false))}, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.log10(((Number) params[0][0]).doubleValue()) / Math.log10(((Number) params[1][0]).doubleValue())};
			}
		});
		
		Functions.registerFunction(new JavaFunction<Number>("sqrt", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.sqrt(((Number) params[0][0]).doubleValue())};
			}
		});
		
		// trigonometry
		
		Functions.registerFunction(new JavaFunction<Number>("sin", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.sin(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		});
		Functions.registerFunction(new JavaFunction<Number>("cos", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.cos(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		});
		Functions.registerFunction(new JavaFunction<Number>("tan", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.tan(Math.toRadians(((Number) params[0][0]).doubleValue()))};
			}
		});
		
		Functions.registerFunction(new JavaFunction<Number>("asin", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.toDegrees(Math.asin(((Number) params[0][0]).doubleValue()))};
			}
		});
		Functions.registerFunction(new JavaFunction<Number>("acos", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.toDegrees(Math.acos(((Number) params[0][0]).doubleValue()))};
			}
		});
		Functions.registerFunction(new JavaFunction<Number>("atan", numberParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				return new Double[] {Math.toDegrees(Math.atan(((Number) params[0][0]).doubleValue()))};
			}
		});
		
		// more stuff
		
		Functions.registerFunction(new JavaFunction<Number>("sum", numbersParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Object[] ns = params[0];
				double sum = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++)
					sum += ((Number) ns[i]).doubleValue();
				return new Double[] {sum};
			}
		});
		Functions.registerFunction(new JavaFunction<Number>("product", numbersParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Object[] ns = params[0];
				double product = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++)
					product *= ((Number) ns[i]).doubleValue();
				return new Double[] {product};
			}
		});
		
		// TODO allow to call a single-arg function that allows multiple values as if it had multiple parameters (or more generally the last parameter?)
		Functions.registerFunction(new JavaFunction<Number>("max", numbersParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Object[] ns = params[0];
				double max = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++) {
					final double d = ((Number) ns[i]).doubleValue();
					if (d > max)
						max = d;
				}
				return new Double[] {max};
			}
		});
		Functions.registerFunction(new JavaFunction<Number>("min", numbersParam, numberClass, true) {
			@Override
			public Number[] execute(final FunctionEvent e, final Object[][] params) {
				final Object[] ns = params[0];
				double min = ((Number) ns[0]).doubleValue();
				for (int i = 1; i < ns.length; i++) {
					final double d = ((Number) ns[i]).doubleValue();
					if (d < min)
						min = d;
				}
				return new Double[] {min};
			}
		});
		
		// misc
		
		// the location expression doesn't work, so why not make a function for the same purpose FIXME document on ExprLocation as well
		Functions.registerFunction(new JavaFunction<Location>("location",
				new Parameter[] {
						new Parameter<Number>("x", numberClass, true, null), new Parameter<Number>("y", numberClass, true, null), new Parameter<Number>("z", numberClass, true, null),
						new Parameter<World>("world", Classes.getExactClassInfo(World.class), true, new EventValueExpression<World>(World.class)),
						new Parameter<Number>("yaw", numberClass, true, new SimpleLiteral<Number>(0, true)), new Parameter<Number>("pitch", numberClass, true, new SimpleLiteral<Number>(0, true))
				}, Classes.getExactClassInfo(Location.class), true) {
			@Override
			public Location[] execute(final FunctionEvent e, final Object[][] params) {
				return new Location[] {new Location((World) params[3][0],
						((Number) params[0][0]).doubleValue(), ((Number) params[1][0]).doubleValue(), ((Number) params[2][0]).doubleValue(),
						((Number) params[4][0]).floatValue(), ((Number) params[5][0]).floatValue())};
			}
		});
		
	}
	
}
