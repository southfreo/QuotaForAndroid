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
 * Class used to represent an operator More specific types of operator will
 * extend this
 * 
 * @author Alex Barfoot
 * 
 */

public class Operator implements Serializable {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -8526367439301015577L;
	/**
	 * The precedence of this operator
	 */
	private int precedence;

	public Operator(int precedence) {
		this.precedence = precedence;
	}

	/**
	 * @return the precedence of this operator
	 */
	public int getPrecedence() {
		return precedence;
	}
}
