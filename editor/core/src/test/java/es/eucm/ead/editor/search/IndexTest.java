/**
 * eAdventure is a research project of the
 *    e-UCM research group.
 *
 *    Copyright 2005-2013 e-UCM research group.
 *
 *    You can access a list of all the contributors to eAdventure at:
 *          http://e-adventure.e-ucm.es/contributors
 *
 *    e-UCM is a research group of the Department of Software Engineering
 *          and Artificial Intelligence at the Complutense University of Madrid
 *          (School of Computer Science).
 *
 *          C Profesor Jose Garcia Santesmases sn,
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

package es.eucm.ead.editor.search;

import com.badlogic.gdx.utils.reflect.Field;
import es.eucm.ead.engine.mock.MockApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author mfreire
 */
public class IndexTest {

	public IndexTest() {
	}

	@Before
	public void setUp() {
		MockApplication.initStatics();
	}

	@After
	public void tearDown() {
	}

	public static class T1 {
		private String indexed = "four, two";
		private String one = "1";
		private String two = "2";
		private String three = "3";
		protected String four = "4";

		public void set(String one, String two, String three, String four) {
			this.one = one;
			this.two = two;
			this.three = three;
			this.four = four;
		}

		@Override
		public String toString() {
			return one + ", " + two + ", " + three + ", " + four;
		}
	}

	public static class T2 {
		private String five = "5";
		private String six = "6";
	}

	public void assertFieldNamesMatch(String[] expected, Field[] found) {
		assertEquals(expected.length, found.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], found[i].getName());
		}
	}

	/**
	 * Test of getIndexedFields method, of class Index.
	 */
	@Test
	public void testGetIdexedFields() throws Exception {
		System.out.println("getIdexedFields");
		Index i = new Index();
		assertFieldNamesMatch(new String[] { "four", "two" },
				i.getIndexedFields(new T1()));
		assertArrayEquals(i.getIndexedFields(new T2()), new Field[0]);
	}

	/**
	 * Test of add method, of class Index.
	 */
	@Test
	public void testAdd() {
		System.out.println("add");

		Index instance = new Index();

		// add a few objects
		T1 a = new T1();
		a.set("a", "b", "c", "d");
		instance.add(a, true);
		T1 b = new T1();
		b.set("u", "w", "x", "y");
		instance.add(b, true);

		// now find them
		assertTrue(instance.search("a").getMatches().isEmpty());
		assertEquals(instance.search("b").getMatches().get(0).getObject(), a);
		assertTrue(instance.search("c").getMatches().isEmpty());
		assertEquals(instance.search("d").getMatches().get(0).getObject(), a);
		assertTrue(instance.search("u").getMatches().isEmpty());
		assertEquals(instance.search("w").getMatches().get(0).getObject(), b);
		assertTrue(instance.search("x").getMatches().isEmpty());
		assertEquals(instance.search("y").getMatches().get(0).getObject(), b);

		// add another b
		T1 bb = new T1();
		bb.set("u", "w", "x", "y");
		instance.add(bb, true);

		// find it
		assertEquals(instance.search("w").getMatches().size(), 2);
		assertEquals(instance.search("y").getMatches().size(), 2);
	}

	/**
	 * Test of remove method, of class Index.
	 */
	@Test
	public void testRemove() {
		System.out.println("add");

		Index instance = new Index();

		// add a few objects
		T1 a = new T1();
		a.set("a", "b", "c", "d");
		instance.add(a, true);
		T1 b = new T1();
		b.set("u", "w", "x", "y");
		instance.add(b, true);

		// and remove the first one
		instance.remove(a, true);

		// now find them
		assertTrue(instance.search("a").getMatches().isEmpty());
		assertTrue(instance.search("b").getMatches().isEmpty());
		assertTrue(instance.search("c").getMatches().isEmpty());
		assertTrue(instance.search("d").getMatches().isEmpty());

		// but the old ones remain
		assertTrue(instance.search("u").getMatches().isEmpty());
		assertEquals(instance.search("w").getMatches().get(0).getObject(), b);
		assertTrue(instance.search("x").getMatches().isEmpty());
		assertEquals(instance.search("y").getMatches().get(0).getObject(), b);
	}

	/**
	 * Test of refresh method, of class Index.
	 */
	@Test
	public void testRefresh() {
		System.out.println("refresh");

		Index instance = new Index();

		// add a few objects
		T1 a = new T1();
		a.set("a", "b", "c", "d");
		instance.add(a, true);
		T1 b = new T1();
		b.set("u", "w", "x", "y");
		instance.add(b, true);

		// now update their values
		a.set("aay", "bay", "cay", "day");
		instance.refresh(a);
		b.set("uay", "way", "xay", "yay");
		instance.refresh(b);

		// the old ones are gone, the new ones here to stay
		assertTrue(instance.search("b").getMatches().isEmpty());
		assertTrue(instance.search("d").getMatches().isEmpty());
		assertTrue(instance.search("w").getMatches().isEmpty());
		assertTrue(instance.search("y").getMatches().isEmpty());
		assertEquals(instance.search("bay").getMatches().get(0).getObject(), a);
		assertEquals(instance.search("day").getMatches().get(0).getObject(), a);
		assertEquals(instance.search("way").getMatches().get(0).getObject(), b);
		assertEquals(instance.search("yay").getMatches().get(0).getObject(), b);
	}
}