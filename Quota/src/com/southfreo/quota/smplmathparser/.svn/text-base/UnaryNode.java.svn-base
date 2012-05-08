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
 * Class representing a unary operator node in a evaluation tree
 * 
 * @author Alex Barfoot
 * 
 */

public class UnaryNode extends EvaluationNode {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1785260805118262581L;
	/**
	 * The UnaryOperator for the operator to be performed
	 */
	private UnaryOperator operator;
	/**
	 * The parameter for this unary operator
	 */
	private EvaluationNode parameter;

	public UnaryNode(UnaryOperator operator, String function,
			EvaluationTree parent) throws MathParserException {
		// set operator
		this.operator = operator;
		String middleStr;
		// get string of function operator is to be applied to
		int start = function.indexOf("(");
		int end = function.lastIndexOf(")");
		if (start > -1 && end > -1
				&& start == operator.getOperatorString().length()) {
			middleStr = function.substring(start + 1, end);
		} else {
			middleStr = function.substring(operator.getOperatorString()
					.length());
		}
		// parse string to get evaluation node operator will be applied to
		parameter = parent.parse(middleStr);
	}

	@Override
	public double evaluate() {
		double parameterValue = parameter.evaluate();
		double value = operator.evaluate(parameterValue);
		return value;
	}
}
