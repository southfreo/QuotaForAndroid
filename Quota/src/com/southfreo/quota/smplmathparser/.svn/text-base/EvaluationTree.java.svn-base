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
 * A class that represents an evaluation tree for a function.
 * 
 * @author Alex Barfoot
 * 
 */

public class EvaluationTree implements Serializable {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 5293967236846252736L;
	/**
	 * The binary operators of this tree
	 */
	private Map<Character, BinaryOperator> binaryOperators = new HashMap<Character, BinaryOperator>();
	/**
	 * The constant names and values for this tree
	 */
	private Map<String, Constant> constants = new HashMap<String, Constant>();
	/**
	 * Value of the tree if it is constant
	 */
	double constantValue;
	/**
	 * The root node of this evaluation tree
	 */
	private EvaluationNode root;
	/**
	 * The unary operators of this tree
	 */
	private Map<String, UnaryOperator> unaryOperators = new HashMap<String, UnaryOperator>();
	/**
	 * The variable names and values for this tree
	 */
	private Map<String, Variable> variables = new HashMap<String, Variable>();

	/**
	 * Initialise and build the evaluation tree
	 * 
	 * @param binaryOperators
	 *            The binary operators to be used by this evaluation tree
	 * @param unaryOperators
	 *            The unary operators to be used by this evaluation tree
	 * @param constants
	 *            The constants to be used by this evaluation tree
	 * @param function
	 *            The string representing the mathematical function to parsed
	 *            into a tree
	 * @throws MathParserException
	 * 
	 */
	public EvaluationTree(Map<Character, BinaryOperator> binaryOperators,
			Map<String, UnaryOperator> unaryOperators,
			Map<String, Constant> constants, String function)
			throws MathParserException {
		// set operators to defaults provided by MathParser
		this.binaryOperators = binaryOperators;
		this.unaryOperators = unaryOperators;
		this.constants = constants;
		// get root node by parsing function
		root = parse(function);
		// check if evaluation will produce constant value
		if (this.isConstant()) {
			// store this constant value
			constantValue = root.evaluate();
		}
	}

	/**
	 * Method that decrements a variable in the evaluation tree by a given value
	 * 
	 * @param name
	 *            The name of the variable to be decremented
	 * @param value
	 *            The value the variable should be decremented by
	 * @throws MathParserException
	 */
	public void decrementVariable(String name, double value)
			throws MathParserException {
		if (variables.containsKey(name)) {
			double currentValue = variables.get(name).getValue();
			variables.get(name).setValue(currentValue - value);
		} else {
			throw new MathParserException("Variable not found: " + name, this
					.toString());
		}
	}

	/**
	 * Evaluate this tree using the currently set variable values
	 * 
	 * @return The result from the evaluation of the tree
	 */
	public double evaluate() {
		if (this.isConstant()) {
			return constantValue;
		} else {
			return root.evaluate();
		}
	}

	/**
	 * Method used to get a list of the constants used in a tree NOTE: this will
	 * include numeric constants as well
	 * 
	 * @return The list of Constants in array form
	 */
	public Constant[] getConstantList() {
		// convert map values to array and return
		Constant[] constantArray = new Constant[constants.size()];
		constantArray = constants.values().toArray(constantArray);
		return constantArray;
	}

	/**
	 * Method used to get a list of the variables used in a tree
	 * 
	 * @return The list of Variables in array form
	 */
	public Variable[] getVariableList() {
		// convert map values to array and return
		Variable[] variableArray = new Variable[variables.size()];
		variableArray = variables.values().toArray(variableArray);
		return variableArray;
	}

	/**
	 * Method that increments a variable in the evaluation tree by a given value
	 * 
	 * @param name
	 *            The name of the variable to be incremented
	 * @param value
	 *            The value the variable should be incremented by
	 * @throws MathParserException
	 */
	public void incrementVariable(String name, double value)
			throws MathParserException {
		if (variables.containsKey(name)) {
			double currentValue = variables.get(name).getValue();
			variables.get(name).setValue(currentValue + value);
		} else {
			throw new MathParserException("Variable not found: " + name, this
					.toString());
		}
	}

	/**
	 * Method to indicate if an evaluation trees value is always constant
	 * 
	 * @return True if evaluation trees values is always constant or false
	 *         otherwise
	 */
	public boolean isConstant() {
		/*
		 * if there only constants and no variables evaluation will provide
		 * constant value
		 */
		boolean isConstant = variables.size() == 0;
		return isConstant;
	}

	/**
	 * Parse a function part and produce a Evaluation node that represents it
	 * This is where the hard work is done :)
	 * 
	 * @param functionPtr
	 *            The function part to be parsed
	 * @return The evaluation node that represents this function part
	 * @throws MathParserException
	 */
	public EvaluationNode parse(String functionPtr) throws MathParserException {
		// the current highest precedence binary operator found
		BinaryOperator binOperator = null;
		// the current highest precedence unary operator found
		UnaryOperator uniOperator = null;
		// the position of the current precedent operator
		int precedentOpPos = -1;
		// position of the last found operator
		int lastOpPos = 0;
		// current bracket depth
		int bracketDepth = 0;
		// array form of the function
		char[] charFunc = functionPtr.toCharArray();
		// loop through the characters in the function
		for (int funcStrIndex = 0; funcStrIndex < charFunc.length; funcStrIndex++) {
			// get current char being parsed
			char currentChar = charFunc[funcStrIndex];
			// check if the current char is a binary operator
			if (binaryOperators.containsKey(currentChar) && bracketDepth == 0) {
				if (funcStrIndex == 0) {
					/*
					 * If operator is at position 0 it must also be a unary one
					 * If this is not the case there must be an error in the
					 * function
					 */
					if (binaryOperators.get(currentChar).isAlsoUnary()) {
						// create unary operator
						UnaryOperator tempUniOperator = unaryOperators
								.get(String.valueOf(currentChar));
						/*
						 * update the position of the last operator
						 */
						lastOpPos = funcStrIndex;
						/*
						 * set highest precedence unary operator to found one if
						 * its precedence is higher or current op is null
						 */
						if (uniOperator == null) {
							uniOperator = tempUniOperator;
							precedentOpPos = funcStrIndex;
						} else if (tempUniOperator.getPrecedence() > uniOperator
								.getPrecedence()) {
							uniOperator = tempUniOperator;
							precedentOpPos = funcStrIndex;
						}
					} else {
						throw new MathParserException(
								"Found binary operator where unary was expected",
								functionPtr);
					}
				} else if (lastOpPos == funcStrIndex - 1 && lastOpPos > 0) {
					/*
					 * If a operator is directly right of a binary operator it
					 * must be unary Its not possible for it to be the most
					 * precedent operator so just set it as the last one
					 */
					lastOpPos = funcStrIndex;
				} else {
					/*
					 * Binary operator has been found Check if it must be set as
					 * the most precedent one
					 */
					lastOpPos = funcStrIndex;
					BinaryOperator tempBinOperator = binaryOperators
							.get(currentChar);
					if (binOperator == null) {
						binOperator = tempBinOperator;
						precedentOpPos = funcStrIndex;
					} else if (tempBinOperator.getPrecedence() > binOperator
							.getPrecedence()) {
						binOperator = tempBinOperator;
						precedentOpPos = funcStrIndex;
					}
				}
			} else if (currentChar == '(') {
				/*
				 * Bracket was found indicating a probable unary operator before
				 * it, e.g. sin, cos, -, etc
				 */
				if (funcStrIndex != 0) {
					/*
					 * As bracket was not at position 0 we can assume the string
					 * between last last found operator and this bracket is a
					 * unary operator
					 */
					if (binOperator == null && bracketDepth == 0) {
						/*
						 * Create unary operator only if the found one is not
						 * located in brackets and a binary operator does not
						 * already exist.
						 */
						String unaryOp = functionPtr.substring(lastOpPos,
								funcStrIndex);
						UnaryOperator tempUniOperator = unaryOperators
								.get(unaryOp);
						if (tempUniOperator != null) {
							if (uniOperator == null) {
								uniOperator = tempUniOperator;
								precedentOpPos = funcStrIndex;
							} else if (tempUniOperator.getPrecedence() > uniOperator
									.getPrecedence()) {
								/*
								 * Don't set if there is currently a unary
								 * operator of higher precedence
								 */
								uniOperator = tempUniOperator;
								precedentOpPos = funcStrIndex;
							}
						} else {
							throw new MathParserException(
									"Unknow unary operator: " + unaryOp,
									functionPtr);
						}
					}
				} else {
					/*
					 * If brackets appears as the first and last characters then
					 * the unary operator + must be inserted to make it a valid
					 * operation
					 */
					if (charFunc[charFunc.length - 1] == ')') {
						UnaryOperator tempUniOperator = unaryOperators.get("+");
						uniOperator = tempUniOperator;
						// function string must be updated with the added +
						functionPtr = "+" + functionPtr;
						funcStrIndex++;
						charFunc = functionPtr.toCharArray();
					}
				}
				// increase bracket depth as an opening one was found
				bracketDepth++;
			} else if (currentChar == ')') {
				// decrease bracket depth as an closing one was found
				bracketDepth--;
			} else if (unaryOperators.containsKey(String.valueOf(currentChar))) {
				/*
				 * This indicates a single character unary operator that is not
				 * a binary one. None of the these exist by default but the user
				 * could define some.
				 */

				// create unary operator
				UnaryOperator tempUniOperator = unaryOperators.get(String
						.valueOf(currentChar));
				/*
				 * update the position of the last operator
				 */
				lastOpPos = funcStrIndex;
				// set found unary operator as precedent one if needed
				if (uniOperator == null) {
					uniOperator = tempUniOperator;
					// update precedent position if needed
					if (binOperator == null) {
						precedentOpPos = funcStrIndex;
					}
				} else if (tempUniOperator.getPrecedence() > uniOperator
						.getPrecedence()) {
					uniOperator = tempUniOperator;
					// update precedent position if needed
					if (binOperator == null) {
						precedentOpPos = funcStrIndex;
					}
				}
			}
		}
		/*
		 * Finished reading function string Create node to represent it from
		 * found operator
		 */
		EvaluationNode node = null;
		if (binOperator != null) {
			node = new BinaryNode(binOperator, precedentOpPos, functionPtr,
					this);
		} else if (uniOperator != null) {
			node = new UnaryNode(uniOperator, functionPtr, this);
		} else {
			// no operator was found so function must be variable or constant
			if (charFunc.length > 0) {
				if (constants.containsKey(functionPtr)) {
					/*
					 * function string is a already defined constant so link
					 * constant node to it
					 */
					Constant con = constants.get(functionPtr);
					node = new ConstantNode(con);
				} else if (MathParser.isNumeric(functionPtr)) {
					// function string an undefined numeric constant
					double conValue = Double.parseDouble(functionPtr);
					Constant con = new Constant(functionPtr, conValue);
					constants.put(functionPtr, con);
					node = new ConstantNode(con);
				} else {
					// function string is a variable
					Variable var;
					if (!variables.containsKey(functionPtr)) {
						// variable does not already exist so create it
						var = new Variable(functionPtr);
						variables.put(functionPtr, var);
					} else {
						// variable already exists so link node to it
						var = variables.get(functionPtr);
					}
					node = new VariableNode(var);
				}
			} else {
				throw new MathParserException("Blank function string",
						functionPtr);
			}
		}
		if (node == null) {
			// there was an error with the function string
			throw new MathParserException(
					"There was an error with the function", functionPtr);
		}
		return node;
	}

	/**
	 * Set a variable in the evaluation tree
	 * 
	 * @param name
	 *            The name of the variable to be set
	 * @param value
	 *            The value to set this variable to
	 * @throws MathParserException
	 */
	public void setVariable(String name, double value)
			throws MathParserException {
		if (variables.containsKey(name)) {
			variables.get(name).setValue(value);
		} else {
			throw new MathParserException("Variable not found: " + name, this
					.toString());
		}
	}
}
