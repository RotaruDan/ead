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
package es.eucm.ead.editor.editorui.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Container;

import es.eucm.ead.editor.editorui.EditorUITest;
import es.eucm.ead.editor.ui.dialogue.ConversationWidget;
import es.eucm.ead.engine.demobuilder.ConversationBuilder;
import es.eucm.ead.engine.demobuilder.ConversationBuilder.ForkBuilder;
import es.eucm.ead.schema.components.behaviors.Behavior;
import es.eucm.ead.schema.components.behaviors.events.Init;
import es.eucm.ead.schema.components.conversation.Conversation;
import es.eucm.ead.schema.effects.Effect;
import es.eucm.ead.schema.effects.TriggerConversation;
import es.eucm.ead.schema.entities.ModelEntity;

public class ConversationWidgetTest extends EditorUITest {

	@Override
	protected void builUI(Group root) {

		Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
		controller.getCommands().pushStack();

		ModelEntity entity = new ModelEntity();
		for (int i = 0; i < 5; ++i) {
			createConversation(entity, i);
		}

		// Create the widget
		ConversationWidget conversEditor = new ConversationWidget(controller);
		conversEditor.prepare(entity);

		Container container = new Container(conversEditor).fill();
		container.setFillParent(true);
		root.addActor(container);
	}

	private void createConversation(ModelEntity entity, int i) {
		String conversationId = "Conversation " + i;
		initBehavior(entity, makeTriggerConversation(conversationId, 0));
		ConversationBuilder conversation = conversation(entity, conversationId);

		ForkBuilder option = conversation
				.speakers("A " + i, "B " + i, "C " + i).start()
				.line(0, i + " - Message 1").line(1, i + " - Question 1")
				.wait(2.5f).options();
		option.start(i + " - Answer 1").line(1, i + " - Response 1")
				.nextNode(0);
		option.start(i + " - Answer 2").line(1, i + " - Response 2")
				.nextNode(0);
	}

	public Behavior initBehavior(ModelEntity parent, Effect... effects) {
		Behavior behavior = new Behavior();
		behavior.setEvent(new Init());
		parent.getComponents().add(behavior);
		for (Effect effect : effects) {
			behavior.getEffects().add(effect);
		}
		return behavior;
	}

	public ConversationBuilder conversation(ModelEntity entity, String id) {
		Conversation conversation = new Conversation();
		entity.getComponents().add(conversation);
		conversation.setId(id);
		return new ConversationBuilder(conversation);
	}

	public TriggerConversation makeTriggerConversation(String conversationId,
			int startingNodeId) {
		TriggerConversation triggerConversation = new TriggerConversation();
		triggerConversation.setNodeId(startingNodeId);
		triggerConversation.setConversationId(conversationId);
		return triggerConversation;
	}

	public static void main(String[] args) {
		new LwjglApplication(new ConversationWidgetTest(),
				"Dialogue Node Widget Test", 720, 480);
	}
}