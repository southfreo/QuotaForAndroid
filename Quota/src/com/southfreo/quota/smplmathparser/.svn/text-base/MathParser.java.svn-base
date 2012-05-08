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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses mathematical expressions to produce a evaluation tree.
 * 
 * @author Alex Barfoot
 * 
 */

public class MathParser implements Serializable {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1352746205327844525L;

	/**
	 * Method used to check if a string is a number
	 * 
	 * @param numStr
	 *            The string to be tested
	 * @return Returns true if string represents a number otherwise false
	 */
	public static boolean isNumeric(String numStr) {
		try {
			Double.parseDouble(numStr);
			return true;
		} catch (NumberFormatException e) {
			// numStr is not numeric
			return false;
		}
	}

	/**
	 * The default binary operators *
	 */
	private Map<Character, BinaryOperator> defaultBinaryOperators = new HashMap<Character, BinaryOperator>();
	/**
	 * The default unary constants
	 */
	private Map<String, Constant> defaultConstants = new HashMap<String, Constant>();

	/**
	 * The default unary operators
	 */
	private Map<String, UnaryOperator> defaultUnaryOperators = new HashMap<String, UnaryOperator>();

	/**
	 * Initialise the math parser
	 */
	public MathParser() {
		DefaultOperators.addDefaultBinaryOperators(this);
		DefaultOperators.addDefaultConstants(this);
		DefaultOperators.addDefaultUnaryOperators(this);
	}

	/**
	 * Add a binary operator to the default set
	 * 
	 * @param binaryOperator
	 *            The binary operator to be added
	 */
	public void addBinaryOperator(BinaryOperator binaryOperator) {
		defaultBinaryOperators.put(binaryOperator.getOperatorChar(),
				binaryOperator);
	}

	/**
	 * Add a constant to the default set
	 * 
	 * @param constant
	 *            The constant to be added
	 */
	public void addConstant(Constant constant) {
		defaultConstants.put(constant.getName(), constant);
	}

	/**
	 * Add a unary operator to the default set
	 * 
	 * @param unaryOperator
	 *            The unary operator to be added
	 */
	public void addUnaryOperator(UnaryOperator unaryOperator) {
		defaultUnaryOperators.put(unaryOperator.getOperatorString(),
				unaryOperator);
	}

	/**
	 * Parses the function to produce an evaluation tree
	 * 
	 * @param function
	 *            String of the function to parsed
	 * @throws MathParserException
	 */
	public EvaluationTree parse(String function) throws MathParserException {
		function = preParse(function);
		EvaluationTree tree = new EvaluationTree(defaultBinaryOperators,
				defaultUnaryOperators, defaultConstants, function);
		return tree;
	}

	/**
	 * Makes the function string ready for parsing
	 * 
	 * @param function
	 *            The string to be made ready for parsing
	 * @return The string preParsed ready for parsing
	 */
	private String preParse(String function) {
		if (function.contains("=")) {
			// strip off = sign and anything before if user has included it
			int equalIndex = function.indexOf('=');
			function = function.substring(equalIndex + 1);
		}
		// trim leading and trailing spaces
		function = function.trim();
		// strip out spaces
		StringBuilder functionBuilder = new StringBuilder();
		for (int i = 0; i < function.length(); i++) {
			if (!(function.charAt(i) == ' ')) {
				functionBuilder.append(function.charAt(i));
			}
		}
		function = functionBuilder.toString();
		return function;
	}
}
