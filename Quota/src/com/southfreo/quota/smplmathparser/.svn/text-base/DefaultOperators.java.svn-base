/*	Copyright (C) 2010 - 2011  Alex Barfoot
 
 	This file is part of SimpleMathParser http://smplmathparse.sourceforge.net/.

    SimpleMathParser is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SimpleMathParser is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SimpleMathParser.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.smplmathparser;

/**
 * Class containing methods that add a default set of operators to a math parser
 * 
 * @author Alex Barfoot
 * 
 */
@SuppressWarnings("serial")
public class DefaultOperators {
	/**
	 * Adds a set of binary operators to the default list of a math parser
	 * 
	 * @param parser
	 *            The parser these binary operators are to be added to
	 */
	public static void addDefaultBinaryOperators(MathParser parser) {
		// addition operator
		parser.addBinaryOperator(new BinaryOperator('+', 500, true) {
			@Override
			public double evaluate(double a, double b) {
				return a + b;
			}
		});
		// subtraction operator
		parser.addBinaryOperator(new BinaryOperator('-', 500, true) {
			@Override
			public double evaluate(double a, double b) {
				return a - b;
			}
		});
		// multiplication operator
		parser.addBinaryOperator(new BinaryOperator('*', 400, false) {
			@Override
			public double evaluate(double a, double b) {
				return a * b;
			}
		});
		// division operator
		parser.addBinaryOperator(new BinaryOperator('/', 400, false) {
			@Override
			public double evaluate(double a, double b) {
				return a / b;
			}
		});
		// modulus operator
		parser.addBinaryOperator(new BinaryOperator('%', 400, false) {
			@Override
			public double evaluate(double a, double b) {
				return a % b;
			}
		});
		// power operator
		parser.addBinaryOperator(new BinaryOperator('^', 300, false) {
			@Override
			public double evaluate(double a, double b) {
				return Math.pow(a, b);
			}
		});
	}

	/**
	 * Adds a set of constants to the default list of a math parser
	 * 
	 * @param parser
	 *            The parser these constants are to be added to
	 */
	public static void addDefaultConstants(MathParser parser) {
		parser.addConstant(new Constant("PI", Math.PI));
		parser.addConstant(new Constant("E", Math.E));
	}

	/**
	 * Adds a set of unary operators to the default list of a math parser
	 * 
	 * @param parser
	 *            The parser these unary operators are to be added to
	 */
	public static void addDefaultUnaryOperators(MathParser parser) {
		// Trigonometric functions
		parser.addUnaryOperator(new UnaryOperator("cos", 200) {
			@Override
			public double evaluate(double a) {
				return Math.cos(a);
			}
		});
		parser.addUnaryOperator(new UnaryOperator("sin", 200) {
			@Override
			public double evaluate(double a) {
				return Math.sin(a);
			}
		});
		parser.addUnaryOperator(new UnaryOperator("tan", 200) {
			@Override
			public double evaluate(double a) {
				return Math.tan(a);
			}
		});
		// inverse trigonometric functions
		parser.addUnaryOperator(new UnaryOperator("acos", 200) {
			@Override
			public double evaluate(double a) {
				return Math.acos(a);
			}
		});
		parser.addUnaryOperator(new UnaryOperator("asin", 200) {
			@Override
			public double evaluate(double a) {
				return Math.asin(a);
			}
		});
		parser.addUnaryOperator(new UnaryOperator("atan", 200) {
			@Override
			public double evaluate(double a) {
				return Math.atan(a);
			}
		});
		// Hyperbolic trigonometric functions
		parser.addUnaryOperator(new UnaryOperator("cosh", 200) {
			@Override
			public double evaluate(double a) {
				return Math.cosh(a);
			}
		});
		parser.addUnaryOperator(new UnaryOperator("sinh", 200) {
			@Override
			public double evaluate(double a) {
				return Math.sinh(a);
			}
		});
		parser.addUnaryOperator(new UnaryOperator("tanh", 200) {
			@Override
			public double evaluate(double a) {
				return Math.tanh(a);
			}
		});
		// exponential operator
		parser.addUnaryOperator(new UnaryOperator("exp", 200) {
			@Override
			public double evaluate(double a) {
				return Math.exp(a);
			}
		});
		// natural log
		parser.addUnaryOperator(new UnaryOperator("ln", 200) {
			@Override
			public double evaluate(double a) {
				return Math.log(a);
			}
		});
		// square root
		parser.addUnaryOperator(new UnaryOperator("sqrt", 200) {
			@Override
			public double evaluate(double a) {
				return Math.sqrt(a);
			}
		});
		// cube root
		parser.addUnaryOperator(new UnaryOperator("cbrt", 200) {
			@Override
			public double evaluate(double a) {
				return Math.cbrt(a);
			}
		});
		// log operator (log10)
		parser.addUnaryOperator(new UnaryOperator("log", 200) {
			@Override
			public double evaluate(double a) {
				return Math.log10(a);
			}
		});
		// unary sign operators
		parser.addUnaryOperator(new UnaryOperator("-", 200) {
			@Override
			public double evaluate(double a) {
				return -a;
			}
		});
		parser.addUnaryOperator(new UnaryOperator("+", 200) {
			@Override
			public double evaluate(double a) {
				return +a;
			}
		});
		parser.addUnaryOperator(new UnaryOperator("abs", 200) {
			@Override
			public double evaluate(double a) {
				return Math.abs(a);
			}
		});
	}
}
