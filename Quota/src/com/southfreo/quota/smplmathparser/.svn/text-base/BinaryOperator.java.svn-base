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
 * Class used to represent a binary operator
 * 
 * @author Alex Barfoot
 * 
 */

public abstract class BinaryOperator extends Operator {	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3523170509932566511L;
	/**
	 * Indicates whether this operator is also a valid unary one
	 */
	private boolean alsoUnary;
	/**
	 * The character that represents this binary operator
	 */
	private char operatorChar;

	/**
	 * Construct a binary operator with the given operator character and
	 * precedence
	 * 
	 * @param operatorChar
	 *            The character that represents this binary operator
	 * @param precedence
	 *            The precedence of this binary operator
	 */
	public BinaryOperator(char operatorChar, int precedence, boolean alsoUnary) {
		super(precedence);
		this.operatorChar = operatorChar;
		this.alsoUnary = alsoUnary;
	}

	/**
	 * Perform the binary on the two given values and return the result
	 * 
	 * @param value1
	 *            The left side argument to the operator
	 * @param value2
	 *            The right side argument to the operator
	 * @return The result of the operator applied to the two input values
	 */
	public abstract double evaluate(double value1, double value2);

	/**
	 * @return the the character that represents this operator
	 */
	public char getOperatorChar() {
		return operatorChar;
	}

	/**
	 * @return returns true if this operator is also a unary one
	 */
	public boolean isAlsoUnary() {
		return alsoUnary;
	}
}
