package es.eucm.ead.editor.ui.dialogue;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.Selection;
import es.eucm.ead.editor.model.Model.FieldListener;
import es.eucm.ead.editor.model.Model.SelectionListener;
import es.eucm.ead.editor.model.events.FieldEvent;
import es.eucm.ead.editor.model.events.SelectionEvent;
import es.eucm.ead.editor.ui.dialogue.node.EffectsNodeWidget;
import es.eucm.ead.editor.ui.dialogue.node.LineNodeWidget;
import es.eucm.ead.editor.ui.dialogue.node.NodeWidget;
import es.eucm.ead.editor.ui.dialogue.node.SimpleNodeWidget;
import es.eucm.ead.editor.ui.dialogue.node.WaitNodeWidget;
import es.eucm.ead.editor.view.widgets.dragndrop.focus.FocusItemList;
import es.eucm.ead.schema.components.conversation.Conversation;
import es.eucm.ead.schema.components.conversation.EffectsNode;
import es.eucm.ead.schema.components.conversation.LineNode;
import es.eucm.ead.schema.components.conversation.Node;
import es.eucm.ead.schema.components.conversation.SimpleNode;
import es.eucm.ead.schema.components.conversation.WaitNode;
import es.eucm.ead.schemax.FieldName;

public class NodesList extends FocusItemList {

	private Controller controller;

	public NodesList(Controller control) {
		super(false);
		this.controller = control;

		controller.getModel().addSelectionListener(new SelectionListener() {

			@Override
			public void modelChanged(SelectionEvent event) {
				if (event.getType() == SelectionEvent.Type.FOCUSED) {
					Object object = event.getSelection()[0];
					if (object instanceof Node) {
						Node node = (Node) object;
						Object conversationObject = controller.getModel()
								.getSelection()
								.getSingle(Selection.CONVERSATION);
						if (conversationObject instanceof Conversation) {
							prepareNodes(node,
									(Conversation) conversationObject);
						}
					}
				}
			}

			@Override
			public boolean listenToContext(String contextId) {
				return Selection.NODE.equals(contextId);
			}
		});
	}

	private void prepareNodes(Node node, Conversation conversation) {
		clear();
		while (node instanceof SimpleNode) {
			SimpleNodeWidget nodeWidget = null;
			if (node instanceof EffectsNode) {
				nodeWidget = new EffectsNodeWidget((EffectsNode) node,
						controller, conversation);
			} else if (node instanceof WaitNode) {
				nodeWidget = new WaitNodeWidget((WaitNode) node, controller,
						conversation);

			} else if (node instanceof LineNode) {
				nodeWidget = new LineNodeWidget((LineNode) node, controller,
						conversation);

			}
			addActor(nodeWidget);

			node = conversation.getNodes().get(
					((SimpleNode) node).getNextNodeId());
		}
	}

	private class NodeFieldListener implements FieldListener {

		private Array<String> nodeFields;
		private int index;

		public NodeFieldListener() {
			nodeFields = new Array<String>(3);
			nodeFields.add(FieldName.NODE_LINE);
			nodeFields.add(FieldName.NODE_SPEAKER);
			nodeFields.add(FieldName.NODE_WAIT_TIME);
		}

		@Override
		public void modelChanged(FieldEvent event) {
			NodeWidget widget = getWidgetFromNode((Node) event.getTarget());
			switch (index) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				break;
			}

		}

		@Override
		public boolean listenToField(String fieldName) {
			index = nodeFields.indexOf(fieldName, false);
			return index != -1;
		}
	}

	private NodeWidget getWidgetFromNode(Node node) {
		for (Actor actor : itemsList.getChildren()) {
			NodeWidget widget = (NodeWidget) actor;
			if (widget.getNode() == node) {
				return widget;
			}
		}
		return null;
	}
}
