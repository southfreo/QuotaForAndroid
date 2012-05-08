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
 * Simple test class
 * 
 * @author Alex Barfoot
 * 
 */

public class MathParserTester {
	public static void main(String[] args) {
		MathParser parser = new MathParser();
		try {
			// test 1
			String function1 = "y = 5";
			EvaluationTree tree1 = parser.parse(function1);
			System.out.println(function1);
			System.out.println(tree1.evaluate());
			System.out.println("Constant: " + tree1.isConstant());
			System.out.println("------");
			// test 2
			String function2 = "y = x";
			EvaluationTree tree2 = parser.parse(function2);
			tree2.setVariable("x", 10.5);
			System.out.println(function2);
			System.out.println(tree2.evaluate());
			tree2.setVariable("x", 34);
			System.out.println(tree2.evaluate());
			System.out.println("Constant: " + tree2.isConstant());
			System.out.println("------");
			// test 3
			String function3 = "y = sin(x)^(z)";
			EvaluationTree tree3 = parser.parse(function3);
			tree3.setVariable("x", 20);
			tree3.setVariable("z", 30);
			System.out.println(function3);
			System.out.println(tree3.evaluate());
			System.out.println("Constant: " + tree3.isConstant());
			System.out.println("------");
		} catch (MathParserException e) {
			e.printStackTrace();
		}
	}
}
