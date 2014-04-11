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

import es.eucm.ead.editor.model.FieldNames;
import es.eucm.ead.editor.control.commands.FieldCommand;
import es.eucm.ead.schema.effects.Transform;
import es.eucm.ead.schema.game.ModelEntity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldCommandTest extends CommandTest {

	@Test
	public void testNormal() {
		ModelEntity game = new ModelEntity();
		game.setInitialScene("old");

		FieldCommand command = new FieldCommand(game, FieldNames.INITIAL_SCENE,
				"new", false);

		command.doCommand();
		assertEquals(game.getInitialScene(), "new");
		command.undoCommand();
		assertEquals(game.getInitialScene(), "old");
		command.doCommand();
		assertEquals(game.getInitialScene(), "new");
	}

	@Test
	public void testFieldFromSuperClass() {
		Transform transform = new Transform();
		transform.setDuration(50);

		FieldCommand command = new FieldCommand(transform, FieldNames.DURATION,
				100, false);

		command.doCommand();
		assertEquals((int) transform.getDuration(), 100);
		command.undoCommand();
		assertEquals((int) transform.getDuration(), 50);
	}

	@Test
	public void testCombine() {
		ModelEntity game = new ModelEntity();
		game.setInitialScene("old");

		FieldCommand command = new FieldCommand(game, FieldNames.INITIAL_SCENE,
				"n", true);
		FieldCommand command2 = new FieldCommand(game,
				FieldNames.INITIAL_SCENE, "ne", true);

		command.doCommand();
		assertEquals(game.getInitialScene(), "n");
		command2.doCommand();
		assertEquals(game.getInitialScene(), "ne");
		command.combine(command2);
		command.undoCommand();
		assertEquals(game.getInitialScene(), "old");

	}
}
