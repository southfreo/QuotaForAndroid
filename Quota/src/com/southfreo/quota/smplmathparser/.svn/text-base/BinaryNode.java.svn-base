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
 * Class representing a binary operator node in a evaluation tree
 * 
 * @author Alex Barfoot
 * 
 */

public class BinaryNode extends EvaluationNode {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1891013317357691674L;
	/**
	 * The BinaryOperator for the operator to be performed
	 */
	private BinaryOperator operator;
	/**
	 * The first parameter for this binary operator
	 */
	private EvaluationNode parameter1;
	/**
	 * The second parameter for this binary operator
	 */
	private EvaluationNode parameter2;

	/**
	 * Construct a binary node with the given operator
	 * 
	 * @param operator
	 *            The binary operator for this node
	 * @param pos
	 *            The position of the binary operator in the function string
	 * @param function
	 *            The function string that contains this binary operator
	 * @param parent
	 *            The parent tree of this operator node
	 * @throws MathParserException
	 */
	public BinaryNode(BinaryOperator operator, int pos, String function,
			EvaluationTree parent) throws MathParserException {

		this.operator = operator;
		// get expression left of operator
		String leftStr = function.substring(0, pos);
		// get expression right of operator
		String rightStr = function.substring(pos + 1, function.length());
		// parse these two expressions
		parameter1 = parent.parse(leftStr);
		parameter2 = parent.parse(rightStr);
	}

	@Override
	public double evaluate() {
		// evaluate parameter one expression
		double parameter1Value = parameter1.evaluate();
		// evaluate parameter two expression
		double parameter2Value = parameter2.evaluate();
		// evaluate operator with these two found values
		double value = operator.evaluate(parameter1Value, parameter2Value);
		return value;
	}
}
