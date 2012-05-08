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
 * Class representing a variable node in a evaluation tree
 * 
 * @author Alex Barfoot
 * 
 */

public class VariableNode extends EvaluationNode {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 7196821894528024L;
	/**
	 * The current variable for this node
	 */
	private Variable value;

	/**
	 * Construct a variable node with the given variable value
	 * 
	 * @param value
	 */
	public VariableNode(Variable value) {
		this.value = value;
	}

	/**
	 * Returns the current value for this variable node
	 */
	@Override
	public double evaluate() {
		return value.getValue();
	}
}
