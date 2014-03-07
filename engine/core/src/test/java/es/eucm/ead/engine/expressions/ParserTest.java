/**
 * eAdventure is a research project of the
 *    e-UCM research group.
 *
 *    Copyright 2005-2014 e-UCM research group.
 *
 *    You can access a list of all the contributors to eAdventure at:
 *          http://e-adventure.e-ucm.es/contributors
 *
 *    e-UCM is a research group of the Department of Software Engineering
 *          and Artificial Intelligence at the Complutense University of Madrid
 *          (School of Computer Science).
 *
 *          CL Profesor Jose Garcia Santesmases 9,
 *          28040 Madrid (Madrid), Spain.
 *
 *          For more info please visit:  <http://e-adventure.e-ucm.es> or
 *          <http://www.e-ucm.es>
 *
 * ****************************************************************************
 *
 *  This file is part of eAdventure
 *
 *      eAdventure is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      eAdventure is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with eAdventure.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.eucm.ead.engine.expressions;

import es.eucm.ead.engine.VarsContext;
import es.eucm.ead.engine.expressions.operators.OperatorFactory;
import es.eucm.ead.schema.components.VariableDef;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * @author mfreire
 */
public class ParserTest {

	private final OperatorFactory operatorRegistry = new OperatorFactory();
	private VarsContext vc = new VarsContext();

	@Before
	public void setUp() {
		vc = new VarsContext();
	}

	private void evalOk(Object expected, String s) {
		try {
			if (expected instanceof Float) {
				assertEquals(s, (Float) expected,
						(Float) Parser.parse(s, operatorRegistry).evaluate(vc),
						0.00001f);
			} else {
				assertEquals(s, expected, Parser.parse(s, operatorRegistry)
						.evaluate(vc));
			}
		} catch (ExpressionEvaluationException ex) {
			fail("Threw unexpected exception " + ex + " for " + s);
		}
	}

	private void parseErr(String s) {
		try {
			Parser.parse(s, operatorRegistry);
		} catch (IllegalArgumentException ex) {
			return;
		}
		fail("Did not throw exception IllegalArgumentException for " + s);
	}

	private void evalErr(String s) {
		try {
			Parser.parse(s, operatorRegistry).evaluate(vc);
		} catch (ExpressionEvaluationException ex) {
			return;
		}
		fail("Did not throw ExpressionException for " + s);
	}

	/**
	 * Test of parse method, of class Parser.
	 */
	@Test
	public void testTypes() {

		// basic types, good
		evalOk((Integer) 1, "i1");
		evalOk(-1.24f, "f-1.24");
		evalOk(false, "bfalse");
		evalOk("hi", "s\"hi\"");

		// basic types, errors
		parseErr("idontknow");
		parseErr("i1.1");
		parseErr("f0E");
		parseErr("feo");
		parseErr("s\"ab\"c\"");

		// boolean gets a pass (this is Boolean.parseBoolean(s) speaking)
		evalOk(false, "b00");
		evalOk(false, "bfalse");
		evalOk(false, "btruesortof");
		evalOk(true, "btrue");

		// complex string quoting
		evalOk("printf(\"hello world\");", "s\"printf(\\\"hello world\\\");\"");
	}

	@Test
	public void testOperators() {

		// string comparisons
		evalOk(true, "(lt s\"abc\" s\"def\")");
		evalOk(false, "(gt s\"abc\" s\"def\")");

		// divide-by-zero, square-root-of-negative
		evalErr("(/ i1 i0)");
		evalOk((Integer) 0, "(/ i0 i1)");
		evalErr("(sqrt i-1)");
		evalOk((Integer) 2, "(sqrt i4)");
		evalOk((float) Math.sqrt(10f), "(sqrt f10)");

		// semi-random tests for dyadic operators
		Random r = new Random(42);
		for (int i = 0; i < 100; i++) {
			int a = r.nextInt(10) + 1;
			int b = r.nextInt(10) + 1;
			evalOk(a < b, "( lt i" + a + " i" + b + ")");
			evalOk(a >= b, "( ge i" + a + " i" + b + ")");
			evalOk(a - b, "( - i" + a + " i" + b + ")");
			evalOk(a + b, "( + i" + a + " i" + b + ")");
			evalOk(a * b, "( * i" + a + " i" + b + ")");
			evalOk(a / b, "( / i" + a + " i" + b + ")");
			evalOk(a % b, "( % i" + a + " i" + b + ")");
		}

		// variadic add, multiply
		evalOk(3.9f, "(+ i1 i2 i3 f-10.10 i5 f6 f-3)");
		evalOk(18, "(* i2 i3 i3)");

		// semi-random tests for logical operators
		for (int i = 0; i < 10; i++) {
			boolean a = r.nextBoolean();
			boolean b = r.nextBoolean();
			evalOk(a != b, "( xor b" + a + " b" + b + ")");
			evalOk(a && b, "( and b" + a + " b" + b + ")");
			evalOk(a || b, "( or b" + a + " b" + b + ")");
			evalOk(!(a && b), "(not ( and b" + a + " b" + b + "))");
			evalOk(!(a || b), "(not ( or b" + a + " b" + b + "))");
		}

		// playing with auto-cast
		evalOk(false, "(eq f1.1 i1)");
		evalOk(false, "(eq i1 f1.1)");
		evalOk(false, "(eq f1 i2)");
		evalOk(true, "(eq f3 i3)");

		// bad types
		evalErr("(+ s\"hi\" f6)");
		// use of and short-circuiting avoids type error
		evalOk(false, "(and (eq i1 f1.1) (or btrue (+ s\"hi\" f6) bfalse))");
		evalOk(true, "(and (eq i1 f1) (or btrue (+ s\"hi\" f6) bfalse))");

		// nested mixed expressions
		evalOk(true, "(and (eq i1 f1.0) (or btrue (eq f5 f6) bfalse))");
		evalOk(false, "(and (eq i1 f1.1) (or btrue (eq f5 f6) bfalse))");

		// same, but with bad types
		evalErr("(+ (eq i1 f1.0) (or btrue (eq f5 f6) bfalse))");

		// same, after casting
		evalOk(2, "(+ (int (eq i1 f1.0)) (int (or btrue (eq f5 f6) bfalse)))");
		evalOk(false,
				"(and (eq i1 f1.1) (or btrue (lt s\"hi\" (string f6)) bfalse))");

		// logical short-circuiting (if evaluated, 1/0 would throw exception)
		evalOk(false,
				"(and (eq i1 f1.1) (or (eq (/ i1 i0) i1) (eq f5 f6) bfalse))");
		evalErr("(and (eq i1 f1) (or (eq (/ i1 i0) i1) (eq f5 f6) bfalse))");
	}

	@Test
	public void testVars() {
		evalErr("(eq $a i10)");
		vc.registerVariable("a", 10);
		evalOk(true, "(eq $a i10)");

		evalErr("(eq $b f20)");
		vc.registerVariable("b", 20f);
		evalOk(20f, "$b");
		evalOk(true, "(eq $b f20)");

		vc.registerVariable("c", false);
		evalOk(false, "$c");
		evalOk(true, "(xor $c btrue)");

		vc.registerVariable("d", "text");
		evalOk("text", "$d");
	}
}
