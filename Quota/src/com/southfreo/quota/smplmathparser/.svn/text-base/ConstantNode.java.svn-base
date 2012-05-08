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
 * Class representing a numerical constant node in a evaluation tree
 * 
 * @author Alex Barfoot
 * 
 */

public class ConstantNode extends EvaluationNode {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6007983239795201053L;
	/**
	 * The current constant for this node
	 */
	private Constant value;

	/**
	 * Construct a constant node with the given constant
	 */
	public ConstantNode(Constant value) {
		this.value = value;
	}

	@Override
	public double evaluate() {
		return value.getValue();
	}
}
