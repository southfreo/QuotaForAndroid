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
 * Class used to represent a unary operator
 * 
 * @author Alex Barfoot
 * 
 */

public abstract class UnaryOperator extends Operator {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1547418889170989250L;
	/**
	 * The string that represents this unary operator
	 */
	private String operatorString;
	/**
	 * The precedence of this unary operator
	 */
	private int precedence;

	/**
	 * Construct a unary operator with the given operator string
	 * 
	 * @param operatorString
	 *            The string that represents this operator
	 */
	public UnaryOperator(String operatorString, int precedence) {
		super(precedence);
		this.operatorString = operatorString;
	}

	/**
	 * Perform the unary operator on the given value
	 * 
	 * @param value
	 *            The value to perform this operator on
	 * @return The result of the operator applied to the input value
	 */
	public abstract double evaluate(double value);

	/**
	 * @return the operatorString
	 */
	public String getOperatorString() {
		return operatorString;
	}

	/**
	 * @return the precedence
	 */
	public int getPrecedence() {
		return precedence;
	}
}
