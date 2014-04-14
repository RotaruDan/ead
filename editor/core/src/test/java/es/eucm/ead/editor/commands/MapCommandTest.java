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
package es.eucm.ead.editor.commands;

import es.eucm.ead.editor.control.commands.MapCommand.PutToMapCommand;
import es.eucm.ead.editor.control.commands.MapCommand.RemoveFromMapCommand;
import es.eucm.ead.schema.entities.ModelEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapCommandTest extends CommandTest {

	private Map<String, ModelEntity> map;

	@Before
	public void setUp() {
		map = new HashMap<String, ModelEntity>();
	}

	@Test
	public void testAdd() {
		ModelEntity scene = new ModelEntity();
		PutToMapCommand command = new PutToMapCommand(map, "scene0", scene);
		command.doCommand();
		assertEquals(map.get("scene0"), scene);
		command.undoCommand();
		assertTrue(map.isEmpty());
	}

	@Test
	public void testSubstitution() {
		ModelEntity oldScene = new ModelEntity();
		ModelEntity newScene = new ModelEntity();
		map.put("scene0", oldScene);
		PutToMapCommand command = new PutToMapCommand(map, "scene0", newScene);
		command.doCommand();
		assertEquals(map.get("scene0"), newScene);
		command.undoCommand();
		assertEquals(map.get("scene0"), oldScene);
	}

	@Test
	public void testRemove() {
		ModelEntity scene = new ModelEntity();
		map.put("scene0", scene);
		RemoveFromMapCommand command = new RemoveFromMapCommand(map, "scene0");
		command.doCommand();
		assertTrue(map.isEmpty());
		command.undoCommand();
		assertEquals(map.get("scene0"), scene);
	}
}
