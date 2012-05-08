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

@SuppressWarnings("serial")
public class MathParserException extends Exception {
	/**
	 * The expression string in which the error occurred
	 */
	public final String expression;

	/**
	 * Construct a new math parser exception with the given error message and
	 * function
	 * 
	 * @param message
	 *            The detail message describing the error
	 * @param expression
	 *            The string of the mathematical expression that caused the
	 *            error
	 */
	public MathParserException(String message, String expression) {
		super(message + " when parsing \"" + expression + "\"");
		this.expression = expression;
	}
}
