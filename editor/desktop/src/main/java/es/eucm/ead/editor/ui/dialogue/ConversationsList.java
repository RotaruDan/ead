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
package es.eucm.ead.editor.ui.dialogue;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.Selection;
import es.eucm.ead.editor.control.actions.model.SetSelection;
import es.eucm.ead.editor.model.Model;
import es.eucm.ead.editor.model.Model.SelectionListener;
import es.eucm.ead.editor.model.events.SelectionEvent;
import es.eucm.ead.editor.view.widgets.dragndrop.focus.FocusItemList.FocusEvent;
import es.eucm.ead.editor.view.widgets.dragndrop.focus.FocusItemList.FocusListener;
import es.eucm.ead.engine.assets.Assets;
import es.eucm.ead.schema.components.ModelComponent;
import es.eucm.ead.schema.components.conversation.Conversation;
import es.eucm.ead.schema.entities.ModelEntity;

/**
 * A widget used to display all the available {@link Conversation} conversations
 * in the {@link Model} in a list.
 * 
 */
public class ConversationsList extends Table {

	private Skin skin;

	private Controller controller;

	private ButtonGroup buttonGroup;

	public ConversationsList(Controller control) {
		this.controller = control;
		buttonGroup = new ButtonGroup();
		Assets assets = controller.getApplicationAssets();
		skin = assets.getSkin();

		addListener(new FocusListener() {

			@Override
			public void focusChanged(FocusEvent event) {
				controller.action(SetSelection.class, null,
						Selection.CONVERSATION,
						((ConversationButton) event.getActor()).conversation);
			}
		});

		controller.getModel().addSelectionListener(new SelectionListener() {

			@Override
			public void modelChanged(SelectionEvent event) {
				if (event.getType() == SelectionEvent.Type.FOCUSED) {
					Object object = event.getSelection()[0];
					if (object instanceof Conversation) {
						Conversation conversation = (Conversation) object;
						Array<Button> buttons = buttonGroup.getButtons();
						for (Button button : buttons) {
							if (((ConversationButton) button).conversation == conversation) {
								button.setChecked(true);
								break;
							}
						}
					}
				}
			}

			@Override
			public boolean listenToContext(String contextId) {
				return Selection.CONVERSATION.equals(contextId);
			}
		});
	}

	public void prepare(ModelEntity entity) {

		buttonGroup.setMinCheckCount(0);
		for (ModelComponent component : entity.getComponents()) {
			if (component instanceof Conversation) {
				Conversation conversation = (Conversation) component;
				addConversationBox(conversation);
			}
		}
		buttonGroup.setMinCheckCount(1);
	}

	public void release() {
		clear();
		buttonGroup.getButtons().clear();
		buttonGroup.getAllChecked().clear();
	}

	private void addConversationBox(Conversation conversation) {
		CheckBox sceneBox = new ConversationButton(controller, conversation,
				skin);
		add(sceneBox).left().expandX();
		buttonGroup.add(sceneBox);
		row();
	}

	private static class ConversationButton extends CheckBox {

		private Conversation conversation;

		public ConversationButton(Controller controller,
				Conversation conversation, Skin skin) {
			super(conversation.getId(), skin);

			this.conversation = conversation;
			Object modelEntity = controller.getModel().getSelection()
					.getSingle(Selection.CONVERSATION);
			setChecked(modelEntity == conversation);
		}

		@Override
		public void setChecked(boolean isChecked) {
			boolean wasChecked = isChecked();
			super.setChecked(isChecked);
			if (!wasChecked && isChecked) {
				fireFocus();
			}
		}

		/**
		 * Fires that this has gained focus
		 */
		private void fireFocus() {
			FocusEvent dropEvent = Pools.obtain(FocusEvent.class);
			dropEvent.setActor(this);
			fire(dropEvent);
			Pools.free(dropEvent);
		}
	}
}
