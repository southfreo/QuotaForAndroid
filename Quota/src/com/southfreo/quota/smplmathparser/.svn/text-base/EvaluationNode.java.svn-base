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

/**
 * Class representing a node in a evaluation tree
 * 
 * @author Alex Barfoot
 * 
 */

public abstract class EvaluationNode implements Serializable {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 2618640894320266872L;
	/**
	 * Indicates whether the node has a constant value or not
	 */
	private boolean constant = false;

	/**
	 * Evaluate this evaluation tree node
	 */
	public abstract double evaluate();

	/**
	 * Returns true if the value of this node will never change so therefore is
	 * constant, returns false otherwise
	 * 
	 * @return false by default, subclass nodes must set constant value to true
	 *         if their value is constant
	 */
	public boolean isConstant() {
		return constant;
	}
}
