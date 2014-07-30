package es.eucm.ead.editor.ui.dialogue.node;

import com.badlogic.gdx.scenes.scene2d.ui.Button;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.schema.components.conversation.Conversation;
import es.eucm.ead.schema.components.conversation.Node;

public abstract class NodeWidget<T extends Node> extends Button {

	protected T node;

	public NodeWidget(T node, Controller controller, Conversation conversation) {
		super(controller.getApplicationAssets().getSkin(), "bubble");
		this.node = node;
		build(controller, conversation);
	}

	protected abstract void build(Controller controller,
			Conversation conversation);

	public T getNode() {
		return node;
	}
}
